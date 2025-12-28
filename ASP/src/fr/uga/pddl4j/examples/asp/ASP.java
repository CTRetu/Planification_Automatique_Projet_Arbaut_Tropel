/*
 * Copyright (c) 2021 by Damien Pellier <Damien.Pellier@imag.fr>.
 *
 * This file is part of PDDL4J library.
 *
 * PDDL4J is free software: you can redistribute it and/or modify * it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * PDDL4J is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License * along with PDDL4J.  If not,
 * see <http://www.gnu.org/licenses/>
 */

package fr.uga.pddl4j.examples.asp;

import fr.uga.pddl4j.heuristics.state.StateHeuristic;
import fr.uga.pddl4j.parser.DefaultParsedProblem;
import fr.uga.pddl4j.parser.RequireKey;
import fr.uga.pddl4j.plan.Plan;
import fr.uga.pddl4j.plan.SequentialPlan;
import fr.uga.pddl4j.planners.AbstractPlanner;
import fr.uga.pddl4j.planners.Planner;
import fr.uga.pddl4j.planners.PlannerConfiguration;
import fr.uga.pddl4j.planners.ProblemNotSupportedException;
import fr.uga.pddl4j.planners.SearchStrategy;
import fr.uga.pddl4j.planners.statespace.search.StateSpaceSearch;
import fr.uga.pddl4j.problem.DefaultProblem;
import fr.uga.pddl4j.problem.Problem;
import fr.uga.pddl4j.problem.State;
import fr.uga.pddl4j.problem.operator.Action;
import fr.uga.pddl4j.problem.operator.ConditionalEffect;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;

/**
 * The class is an example. It shows how to create a simple A* search planner able to
 * solve an ADL problem by choosing the heuristic to used and its weight.
 *
 * @author D. Pellier
 * @version 4.0 - 30.11.2021
 */
@CommandLine.Command(name = "ASP",
    version = "ASP 1.0",
    description = "Solves a specified planning problem using A* search strategy.",
    sortOptions = false,
    mixinStandardHelpOptions = true,
    headerHeading = "Usage:%n",
    synopsisHeading = "%n",
    descriptionHeading = "%nDescription:%n%n",
    parameterListHeading = "%nParameters:%n",
    optionListHeading = "%nOptions:%n")
public class ASP extends AbstractPlanner {

    /**
     * The class logger.
     */
    private static final Logger LOGGER = LogManager.getLogger(ASP.class.getName());

    /**
     * The HEURISTIC property used for planner configuration.
     */
    public static final String HEURISTIC_SETTING = "HEURISTIC";

    /**
     * The default value of the HEURISTIC property used for planner configuration.
     */
    public static final StateHeuristic.Name DEFAULT_HEURISTIC = StateHeuristic.Name.FAST_FORWARD;

    /**
     * The WEIGHT_HEURISTIC property used for planner configuration.
     */
    public static final String WEIGHT_HEURISTIC_SETTING = "WEIGHT_HEURISTIC";

    /**
     * The default value of the WEIGHT_HEURISTIC property used for planner configuration.
     */
    public static final double DEFAULT_WEIGHT_HEURISTIC = 1.0;

    /**
     * The weight of the heuristic.
     */
    private double heuristicWeight;

    /**
     * The name of the heuristic used by the planner.
     */
    private StateHeuristic.Name heuristic;

    /**
     * Statistics for Monte Carlo search.
     */
    private int montecarloNodesExplored = 0;

    /**
     * Creates a new A* search planner with the default configuration.
     */
    public ASP() {
        this(ASP.getDefaultConfiguration());
    }

    /**
     * Creates a new A* search planner with a specified configuration.
     *
     * @param configuration the configuration of the planner.
     */
    public ASP(final PlannerConfiguration configuration) {
        super();
        this.setConfiguration(configuration);
    }

    /**
     * Sets the weight of the heuristic.
     *
     * @param weight the weight of the heuristic. The weight must be greater than 0.
     * @throws IllegalArgumentException if the weight is strictly less than 0.
     */
    @CommandLine.Option(names = {"-w", "--weight"}, defaultValue = "1.0",
        paramLabel = "<weight>", description = "Set the weight of the heuristic (preset 1.0).")
    public void setHeuristicWeight(final double weight) {
        if (weight <= 0) {
            throw new IllegalArgumentException("Weight <= 0");
        }
        this.heuristicWeight = weight;
    }

    /**
     * Set the name of heuristic used by the planner to the solve a planning problem.
     *
     * @param heuristic the name of the heuristic.
     */
    @CommandLine.Option(names = {"-e", "--heuristic"}, defaultValue = "FAST_FORWARD",
        description = "Set the heuristic : AJUSTED_SUM, AJUSTED_SUM2, AJUSTED_SUM2M, COMBO, "
            + "MAX, FAST_FORWARD SET_LEVEL, SUM, SUM_MUTEX (preset: FAST_FORWARD)")
    public void setHeuristic(StateHeuristic.Name heuristic) {
        this.heuristic = heuristic;
    }

    /**
     * Returns the name of the heuristic used by the planner to solve a planning problem.
     *
     * @return the name of the heuristic used by the planner to solve a planning problem.
     */
    public final StateHeuristic.Name getHeuristic() {
        return this.heuristic;
    }

    /**
     * Returns the weight of the heuristic.
     *
     * @return the weight of the heuristic.
     */
    public final double getHeuristicWeight() {
        return this.heuristicWeight;
    }

    /**
     * Instantiates the planning problem from a parsed problem.
     *
     * @param problem the problem to instantiate.
     * @return the instantiated planning problem or null if the problem cannot be instantiated.
     */
    @Override
    public Problem instantiate(DefaultParsedProblem problem) {
        final Problem pb = new DefaultProblem(problem);
        pb.instantiate();
        return pb;
    }

    /**
     * Search a solution plan to a specified domain and problem using A*.
     *
     * @param problem the problem to solve.
     * @return the plan found or null if no plan was found.
     */
    @Override
    public Plan solve(final Problem problem) {
        LOGGER.info("========================================\n");
        LOGGER.info("= COMPARISON: A* vs Monte Carlo Search =\n");
        LOGGER.info("========================================\n\n");
        
        // ===== A* SEARCH =====
        LOGGER.info("--- Testing A* Search ---\n");
        StateSpaceSearch search = StateSpaceSearch.getInstance(SearchStrategy.Name.ASTAR,
            this.getHeuristic(), this.getHeuristicWeight(), this.getTimeout());
        LOGGER.info("* Starting A* search\n");
        
        final long astarStartTime = System.currentTimeMillis();
        Plan astarPlan = search.searchPlan(problem);
        final long astarEndTime = System.currentTimeMillis();
        final double astarTime = (astarEndTime - astarStartTime) / 1000.0;
        
        if (astarPlan != null) {
            LOGGER.info("* A* search succeeded\n");
            LOGGER.info("* A* plan length: " + astarPlan.size() + " actions\n");
            LOGGER.info("* A* search time: " + astarTime + " seconds\n");
            LOGGER.info("* A* memory used: " + search.getMemoryUsed() + " KB\n");
        } else {
            LOGGER.info("* A* search failed\n");
        }
        
        LOGGER.info("\n");
        
        // ===== MONTE CARLO SEARCH =====
        LOGGER.info("--- Testing Monte Carlo Search ---\n");
        Plan montecarloPlan = null;
        double montecarloTime = 0;
        int mcNodesExplored = 0;
        
        try {
            final long mcStartTime = System.currentTimeMillis();
            montecarloPlan = this.montecarlo(problem);
            final long mcEndTime = System.currentTimeMillis();
            montecarloTime = (mcEndTime - mcStartTime) / 1000.0;
            mcNodesExplored = this.montecarloNodesExplored;
            
            if (montecarloPlan != null) {
                LOGGER.info("* Monte Carlo plan length: " + montecarloPlan.size() + " actions\n");
                LOGGER.info("* Monte Carlo search time: " + montecarloTime + " seconds\n");
                LOGGER.info("* Monte Carlo simulations: " + mcNodesExplored + "\n");
            }
        } catch (ProblemNotSupportedException e) {
            LOGGER.error("* Monte Carlo search failed: " + e.getMessage() + "\n");
        }
        
        LOGGER.info("\n");
        
        // ===== COMPARISON SUMMARY =====
        LOGGER.info("========================================\n");
        LOGGER.info("= COMPARATIVE SUMMARY =\n");
        LOGGER.info("========================================\n");
        LOGGER.info("\nAlgorithm         | Status  | Plan Length | Time (s) | Nodes/Simulations\n");
        LOGGER.info("------------------|---------|-------------|----------|------------------\n");
        LOGGER.info(String.format("A*                | %-7s | %-11s | %-8.3f | %d\n",
            astarPlan != null ? "SUCCESS" : "FAILED",
            astarPlan != null ? astarPlan.size() + " actions" : "N/A",
            astarTime,
            search.getExploredNodes()));
        LOGGER.info(String.format("Monte Carlo       | %-7s | %-11s | %-8.3f | %d\n",
            montecarloPlan != null ? "SUCCESS" : "FAILED",
            montecarloPlan != null ? montecarloPlan.size() + " actions" : "N/A",
            montecarloTime,
            mcNodesExplored));
        LOGGER.info("========================================\n");
        
        if (astarPlan != null && montecarloPlan != null) {
            LOGGER.info("\n--- Winner Analysis ---\n");
            LOGGER.info("Shortest plan: " + (astarPlan.size() <= montecarloPlan.size() ? "A* (" + astarPlan.size() + " actions)" : "Monte Carlo (" + montecarloPlan.size() + " actions)") + "\n");
            LOGGER.info("Fastest search: " + (astarTime <= montecarloTime ? "A* (" + String.format("%.3f", astarTime) + "s)" : "Monte Carlo (" + String.format("%.3f", montecarloTime) + "s)") + "\n");
            
            double planQualityDiff = Math.abs(astarPlan.size() - montecarloPlan.size());
            double timeDiff = Math.abs(astarTime - montecarloTime);
            LOGGER.info("Plan length difference: " + (int)planQualityDiff + " actions\n");
            LOGGER.info("Time difference: " + String.format("%.3f", timeDiff) + " seconds\n");
        } else if (astarPlan != null) {
            LOGGER.info("\nOverall winner: A* (only successful algorithm)\n");
        } else if (montecarloPlan != null) {
            LOGGER.info("\nOverall winner: Monte Carlo (only successful algorithm)\n");
        } else {
            LOGGER.info("\nBoth algorithms failed to find a solution.\n");
        }
        LOGGER.info("========================================\n");
        
        // Export results to CSV for graphical analysis
        exportResultsToCSV(astarPlan, montecarloPlan, astarTime, montecarloTime,
                          search.getExploredNodes(), mcNodesExplored);
        
        // Update statistics with A* results
        if (astarPlan != null) {
            this.getStatistics().setTimeToSearch(search.getSearchingTime());
            this.getStatistics().setMemoryUsedToSearch(search.getMemoryUsed());
        }
        
        // Return A* plan as the primary result (or Monte Carlo if A* failed)
        return astarPlan != null ? astarPlan : montecarloPlan;
    }
    
    /**
     * Export comparison results to CSV file for visualization.
     *
     * @param astarPlan the plan found by A*
     * @param montecarloPlan the plan found by Monte Carlo
     * @param astarTime time taken by A*
     * @param montecarloTime time taken by Monte Carlo
     * @param astarNodes nodes explored by A*
     * @param mcSimulations simulations performed by Monte Carlo
     */
    private void exportResultsToCSV(Plan astarPlan, Plan montecarloPlan,
                                    double astarTime, double montecarloTime,
                                    int astarNodes, int mcSimulations) {
        try {
            PrintWriter csvWriter = new PrintWriter(new FileWriter("comparison_results.csv"));
            csvWriter.println("Algorithm,Plan_Length,Time_Seconds,Nodes_Simulations,Success");
            csvWriter.println("A*," +
                (astarPlan != null ? astarPlan.size() : 0) + "," +
                astarTime + "," +
                astarNodes + "," +
                (astarPlan != null ? "1" : "0"));
            csvWriter.println("Monte_Carlo," +
                (montecarloPlan != null ? montecarloPlan.size() : 0) + "," +
                montecarloTime + "," +
                mcSimulations + "," +
                (montecarloPlan != null ? "1" : "0"));
            csvWriter.close();
            
            LOGGER.info("\n* Results exported to comparison_results.csv\n");
            LOGGER.info("* Run 'python compare_algorithms.py' to generate charts\n");
            
        } catch (IOException e) {
            LOGGER.error("Failed to export results: " + e.getMessage() + "\n");
        }
    }

    /**
     * Checks the planner configuration and returns if the configuration is valid.
     * A configuration is valid if (1) the domain and the problem files exist and
     * can be read, (2) the timeout is greater than 0, (3) the weight of the
     * heuristic is greater than 0 and (4) the heuristic is a not null.
     *
     * @return <code>true</code> if the configuration is valid <code>false</code> otherwise.
     */
    public boolean hasValidConfiguration() {
        return super.hasValidConfiguration()
            && this.getHeuristicWeight() > 0.0
            && this.getHeuristic() != null;
    }

    /**
     * This method return the default arguments of the planner.
     *
     * @return the default arguments of the planner.
     * @see PlannerConfiguration
     */
    public static PlannerConfiguration getDefaultConfiguration() {
        PlannerConfiguration config = Planner.getDefaultConfiguration();
        config.setProperty(ASP.HEURISTIC_SETTING, ASP.DEFAULT_HEURISTIC.toString());
        config.setProperty(ASP.WEIGHT_HEURISTIC_SETTING,
            Double.toString(ASP.DEFAULT_WEIGHT_HEURISTIC));
        return config;
    }

    /**
     * Returns the configuration of the planner.
     *
     * @return the configuration of the planner.
     */
    @Override
    public PlannerConfiguration getConfiguration() {
        final PlannerConfiguration config = super.getConfiguration();
        config.setProperty(ASP.HEURISTIC_SETTING, this.getHeuristic().toString());
        config.setProperty(ASP.WEIGHT_HEURISTIC_SETTING, Double.toString(this.getHeuristicWeight()));
        return config;
    }

    /**
     * Sets the configuration of the planner. If a planner setting is not defined in
     * the specified configuration, the setting is initialized with its default value.
     *
     * @param configuration the configuration to set.
     */
    @Override
    public void setConfiguration(final PlannerConfiguration configuration) {
        super.setConfiguration(configuration);
        if (configuration.getProperty(ASP.WEIGHT_HEURISTIC_SETTING) == null) {
            this.setHeuristicWeight(ASP.DEFAULT_WEIGHT_HEURISTIC);
        } else {
            this.setHeuristicWeight(Double.parseDouble(configuration.getProperty(
                ASP.WEIGHT_HEURISTIC_SETTING)));
        }
        if (configuration.getProperty(ASP.HEURISTIC_SETTING) == null) {
            this.setHeuristic(ASP.DEFAULT_HEURISTIC);
        } else {
            this.setHeuristic(StateHeuristic.Name.valueOf(configuration.getProperty(
                ASP.HEURISTIC_SETTING)));
        }
    }

    
    /**
     * The main method of the <code>ASP</code> planner.
     *
     * @param args the arguments of the command line.
     */
    public static void main(String[] args) {
        try {
            final ASP planner = new ASP();
            CommandLine cmd = new CommandLine(planner);
            cmd.execute(args);
        } catch (IllegalArgumentException e) {
            LOGGER.fatal(e.getMessage());
        }
    }

    /**
     * Search a solution plan for a planning problem using an A* search strategy.
     *
     * @param problem the problem to solve.
     * @return a plan solution for the problem or null if there is no solution
     * @throws ProblemNotSupportedException if the problem to solve is not supported by the planner.
     */
    public Plan astar(Problem problem) throws ProblemNotSupportedException {
        // Check if the problem is supported by the planner
        if (!this.isSupported(problem)) {
            throw new ProblemNotSupportedException("Problem not supported");
        }

        // First we create an instance of the heuristic to use to guide the search
        final StateHeuristic heuristic = StateHeuristic.getInstance(this.getHeuristic(), problem);

        // We get the initial state from the planning problem
        final State init = new State(problem.getInitialState());

        // We initialize the closed list of nodes (store the nodes explored)
        final Set<Node> close = new HashSet<>();

        // We initialize the opened list to store the pending node according to function f
        final double weight = this.getHeuristicWeight();
        final PriorityQueue<Node> open = new PriorityQueue<>(100, new Comparator<Node>() {
            public int compare(Node n1, Node n2) {
                double f1 = weight * n1.getHeuristic() + n1.getCost();
                double f2 = weight * n2.getHeuristic() + n2.getCost();
                return Double.compare(f1, f2);
            }
        });

        // We create the root node of the tree search
        final Node root = new Node(init, null, -1, 0, heuristic.estimate(init, problem.getGoal()));

        // We add the root to the list of pending nodes
        open.add(root);
        Plan plan = null;

        // We set the timeout in ms allocated to the search
        final int timeout = this.getTimeout() * 1000;
        long time = 0;

        // We start the search
        while (!open.isEmpty() && plan == null && time < timeout) {

            // We pop the first node in the pending list open
            final Node current = open.poll();
            close.add(current);

            // If the goal is satisfied in the current node then extract the search and return it
            if (current.satisfy(problem.getGoal())) {
                return this.extractPlan(current, problem);
            } else { // Else we try to apply the actions of the problem to the current node
                for (int i = 0; i < problem.getActions().size(); i++) {
                    // We get the actions of the problem
                    Action a = problem.getActions().get(i);
                    // If the action is applicable in the current node
                    if (a.isApplicable(current)) {
                        Node next = new Node(current);
                        // We apply the effect of the action
                        final List<ConditionalEffect> effects = a.getConditionalEffects();
                        for (ConditionalEffect ce : effects) {
                            if (current.satisfy(ce.getCondition())) {
                                next.apply(ce.getEffect());
                            }
                        }
                        // We set the new child node information
                        final double g = current.getCost() + 1;
                        if (!close.contains(next)) {
                            next.setCost(g);
                            next.setParent(current);
                            next.setAction(i);
                            next.setHeuristic(heuristic.estimate(next, problem.getGoal()));
                            open.add(next);
                        }
                    }
                }
            }
        }

        // Finally, we return the search computed or null if no search was found
        return plan;
    }

    /**
     * Extracts a search from a specified node.
     *
     * @param node    the node.
     * @param problem the problem.
     * @return the search extracted from the specified node.
     */
    private Plan extractPlan(final Node node, final Problem problem) {
        Node n = node;
        final Plan plan = new SequentialPlan();
        while (n.getAction() != -1) {
            final Action a = problem.getActions().get(n.getAction());
            plan.add(0, a);
            n = n.getParent();
        }
        return plan;
    }

    /**
     * Search a solution plan for a planning problem using a Monte Carlo search strategy.
     *
     * @param problem the problem to solve.
     * @return a plan solution for the problem or null if there is no solution
     * @throws ProblemNotSupportedException if the problem to solve is not supported by the planner.
     */
    public Plan montecarlo(Problem problem) throws ProblemNotSupportedException {
        // Check if the problem is supported by the planner
        if (!this.isSupported(problem)) {
            throw new ProblemNotSupportedException("Problem not supported");
        }

        // Random generator for Monte Carlo simulations
        final Random random = new Random();

        // We get the initial state from the planning problem
        final State init = new State(problem.getInitialState());

        // Variables to track the best solution found
        Plan bestPlan = null;
        int bestPlanLength = Integer.MAX_VALUE;

        // We set the timeout in ms allocated to the search
        final int timeout = this.getTimeout() * 1000;
        final long startTime = System.currentTimeMillis();
        
        // Number of simulations to perform
        final int maxSimulations = 100000;
        int simulationCount = 0;

        LOGGER.info("* Starting Monte Carlo search\n");

        // Perform multiple Monte Carlo simulations
        while (simulationCount < maxSimulations && System.currentTimeMillis() - startTime < timeout) {
            simulationCount++;

            // Perform a single Monte Carlo rollout
            Plan candidatePlan = performRollout(problem, init, random, timeout - (int)(System.currentTimeMillis() - startTime));

            // If we found a valid plan, check if it's better than the current best
            if (candidatePlan != null) {
                int planLength = candidatePlan.size();
                if (planLength < bestPlanLength) {
                    bestPlan = candidatePlan;
                    bestPlanLength = planLength;
                    LOGGER.info("* Found plan with length: " + planLength + " (simulation " + simulationCount + ")\n");
                }
            }
        }

        LOGGER.info("* Monte Carlo search completed after " + simulationCount + " simulations\n");
        if (bestPlan != null) {
            LOGGER.info("* Best plan length: " + bestPlanLength + "\n");
        } else {
            LOGGER.info("* No plan found\n");
        }

        // Store the number of nodes explored (simulations performed)
        this.montecarloNodesExplored = simulationCount;

        return bestPlan;
    }

    /**
     * Performs a single Monte Carlo rollout from the initial state.
     *
     * @param problem the planning problem.
     * @param init the initial state.
     * @param random the random generator.
     * @param remainingTime the remaining time in milliseconds.
     * @return a plan if the goal is reached, null otherwise.
     */
    private Plan performRollout(Problem problem, State init, Random random, int remainingTime) {
        final long startTime = System.currentTimeMillis();
        final int maxDepth = 100; // Maximum depth for a single rollout
        
        State currentState = new State(init);
        List<Integer> actionSequence = new ArrayList<>();

        // Perform random walk until goal is reached or max depth is reached
        for (int depth = 0; depth < maxDepth; depth++) {
            // Check timeout
            if (remainingTime > 0 && System.currentTimeMillis() - startTime > remainingTime) {
                return null;
            }

            // Check if goal is reached
            if (currentState.satisfy(problem.getGoal())) {
                // Extract the plan from the action sequence
                Plan plan = new SequentialPlan();
                for (Integer actionIndex : actionSequence) {
                    Action a = problem.getActions().get(actionIndex);
                    plan.add(plan.size(), a);
                }
                return plan;
            }

            // Get all applicable actions
            List<Integer> applicableActions = new ArrayList<>();
            for (int i = 0; i < problem.getActions().size(); i++) {
                Action a = problem.getActions().get(i);
                if (a.isApplicable(currentState)) {
                    applicableActions.add(i);
                }
            }

            // If no applicable actions, rollout failed
            if (applicableActions.isEmpty()) {
                return null;
            }

            // Choose a random applicable action
            int randomActionIndex = applicableActions.get(random.nextInt(applicableActions.size()));
            Action selectedAction = problem.getActions().get(randomActionIndex);

            // Apply the action to get the next state
            State nextState = new State(currentState);
            final List<ConditionalEffect> effects = selectedAction.getConditionalEffects();
            for (ConditionalEffect ce : effects) {
                if (currentState.satisfy(ce.getCondition())) {
                    nextState.apply(ce.getEffect());
                }
            }

            // Update current state and action sequence
            currentState = nextState;
            actionSequence.add(randomActionIndex);
        }

        // Max depth reached without finding goal
        return null;
    }

    /**
     * Returns if a specified problem is supported by the planner. Just ADL problem can be solved by this planner.
     *
     * @param problem the problem to test.
     * @return <code>true</code> if the problem is supported <code>false</code> otherwise.
     */
    @Override
    public boolean isSupported(Problem problem) {
        return (problem.getRequirements().contains(RequireKey.ACTION_COSTS)
            || problem.getRequirements().contains(RequireKey.CONSTRAINTS)
            || problem.getRequirements().contains(RequireKey.CONTINOUS_EFFECTS)
            || problem.getRequirements().contains(RequireKey.DERIVED_PREDICATES)
            || problem.getRequirements().contains(RequireKey.DURATIVE_ACTIONS)
            || problem.getRequirements().contains(RequireKey.DURATION_INEQUALITIES)
            || problem.getRequirements().contains(RequireKey.FLUENTS)
            || problem.getRequirements().contains(RequireKey.GOAL_UTILITIES)
            || problem.getRequirements().contains(RequireKey.METHOD_CONSTRAINTS)
            || problem.getRequirements().contains(RequireKey.NUMERIC_FLUENTS)
            || problem.getRequirements().contains(RequireKey.OBJECT_FLUENTS)
            || problem.getRequirements().contains(RequireKey.PREFERENCES)
            || problem.getRequirements().contains(RequireKey.TIMED_INITIAL_LITERALS)
            || problem.getRequirements().contains(RequireKey.HIERARCHY))
            ? false : true;
    }
}