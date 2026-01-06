# Projet de Planification Automatique : MCTS (Monte Carlo Tree Search)
 
**Auteurs :** Jean-Baptiste ARBAUT, Célia TROPEL
**Formation :** Master 2 MIASHS, Parcours Informatique et Cognition
**Module :** Planification Automatique  
**Année :** 2025-2026  

---


## Table des matières

1. [Description du projet](#description-du-projet)
2. [Architecture du projet](#architecture-du-projet)
   1. [Composants Principaux](#composants-principaux)
      1. [ASP.java - Planificateur Hybride](#1-aspjava---planificateur-hybride)
      2. [compare_algorithms.py - Script de Visualisation](#2-compare_algorithmspy---script-de-visualisation)
3. [Implémentation](#implémentation)
   1. [Algorithme A* (Baseline fournie)](#algorithme-a-baseline-fournie)
   2. [Algorithme Monté Carlo avec Pure Random Walks](#algorithme-monté-carlo-avec-pure-random-walks)
4. [Script de Comparaison](#script-de-comparaison)
   1. [Fonctionnement de compare_algorithms.py](#fonctionnement-de-compare_algorithmspy)
   2. [Format CSV](#format-csv)
5. [Compilation et Exécution](#compilation-et-exécution)
   1. [Prérequis](#prérequis)
   2. [Installation des dépendances Python](#installation-des-dépendances-python)
   3. [Compilation](#compilation)
   4. [Exécution](#exécution)
6. [Résultats et Analyse](#résultats-et-analyse)
   1. [Synthèse Globale](#synthèse-globale)
   2. [Nombre de Problèmes Résolus](#nombre-de-problèmes-résolus)
   3. [Performance du temps d'exécution (Runtime)](#performance-du-temps-dexécution-runtime)
   4. [Qualité des Plans (Makespan)](#qualité-des-plans-makespan)
   5. [Analyse par Domaine](#analyse-par-domaine)
      - [Blocksworld](#1-blocksworld)
      - [Depots](#2-depots)
      - [Freecells](#3-freecells)
      - [Logistics](#4-logistics)
   6. [Conclusion Générale](#conclusion-générale)
7. [Annexe : Résultats détaillés par domaine](#annexe--résultats-détaillés-par-domaine)
   - [Blocksworld](#blocksworld)
   - [Depots](#depots)
   - [Freecells](#freecells)
   - [Logistics](#logistics)
8. [Contributeurs](#contributeurs)


---

## Description du Projet

Ce projet implémente un planificateur basé sur **Monte Carlo Tree Search (MCTS)** avec la technique de **Pure Random Walks** en utilisant la bibliothèque PDDL4J. L'objectif est de comparer les performances de cet algorithme stochastique avec le planificateur A* (HSP) de PDDL4J sur quatre domaines de planification classiques.

**Les objectifs du projet sont les suivants :**   
- Implémenter un planificateur MCTS avec Pure Random Walks en Java.
- Comparer MCTS avec A* (HSP) sur 4 benchmarks : **Blocksworld**, **Depots**, **Freecells**, **Logistics**.
- Évaluer deux métriques : **Runtime** (temps d'exécution) et **Makespan** (longueur du plan).
- Générer des visualisations graphiques des résultats et les interpréter dans ce readme.

---

## Architecture du Projet

```
ASP/
│
├── src/fr/uga/pddl4j/examples/asp/
│   ├── ASP.java              # Planificateur principal (A* + MCTS)
│   └── Node.java              # Classe représentant un nœud de recherche
│
├── classes/                   # Fichiers .class compilés
│
├── lib/                       # Bibliothèques PDDL4J
│
├── blocks/                    # Domaine Blocksworld
│   ├── domain-b.pddl
│   └── p001.pddl ... p010.pddl
│
├── depots/                    # Domaine Depots
│   ├── domain-d.pddl
│   └── p01.pddl ... p10.pddl
│
├── freecells/                 # Domaine Freecells
│   ├── domain-f.pddl
│   └── p01.pddl ... p10.pddl
│
├── logistics/                 # Domaine Logistics
│   ├── domain-l.pddl
│   └── p01.pddl ... p10.pddl
│
├── compare_algorithms.py      # Script de comparaison et visualisation
├── comparison_results.csv     # Résultats exportés (généré automatiquement par ASP.java)  
└── README.md                  # Ce fichier
```

### Composants Principaux

#### 1. **ASP.java** - Planificateur Hybride
Fichier central qui implémente deux stratégies de planification :

- **Méthode `astar()`** : Algorithme A* avec heuristique (Fast-Forward).
- **Méthode `montecarlo()`** : Monte Carlo Tree Search (MCTS) avec Pure Random Walks.
- **Méthode `solve()`** : Compare les deux algorithmes et exporte les résultats.
- **Méthode `performRollout()`** : Effectue une simulation aléatoire (rollout).

Le fichier génère un CSV `comparison_results.csv` contenant les résultats pour chaque algorithme et chaque problème testé.

#### 2. **compare_algorithms.py** - Script de Visualisation
Ce script Python :
- Charge les résultats depuis `comparison_results.csv`.
- Génère des graphiques comparatifs (Runtime, Makespan).
- Produit des statistiques détaillées.
- Ordonne les problèmes par difficulté croissante.

---

## Implémentation

### Algorithme A* (Baseline fournie)

L’algorithme A* utilisé dans ce projet est celui fourni par notre professeur, il est de la bibliothèque PDDL4J. Il sert de référence pour évaluer les performances de notre implémentation Monte Carlo. A* repose sur une recherche informée guidée par une heuristique (Fast-Forward) et utilise une fonction d’évaluation : `f(n) = g(n) + w·h(n)`.

### Algorithme Monté Carlo avec Pure Random Walks

Le planificateur Monté Carlo a été entièrement implémenté par nos soins conformément aux consignes du projet. Il repose sur une approche de pure random walks, sans heuristique ni stratégie de sélection avancée.

Le principe est le suivant :
- À partir de l’état initial, l’algorithme effectue des simulations aléatoires (rollouts).
- À chaque étape, une action applicable est choisie uniformément au hasard.
- Un rollout s’arrête lorsque :
  - L’état but est atteint (succès).
  - Aucune action n’est applicable.
  - Ou une profondeur maximale est atteinte (échec).
Le meilleur plan trouvé avant la fin du temps imparti est conservé.

#### Paramètres principaux : 

- **Timeout** : 300 secondes (5 minutes).
- **Nombre maximal de simulations** : 100 000.
- **Profondeur maximale par rollout** : 100 actions.
- **Stratégie de sélection** : Uniforme (Pure Random).

---

## Script de Comparaison

### Fonctionnement de `compare_algorithms.py`

Le script `compare_algorithms.py` automatise la comparaison entre A* et Monte Carlo à partir des résultats générés par le planificateur Java.

Le script réalise les étapes suivantes :
1. **Chargement des résultats** depuis le fichier `comparison_results.csv`.
2. **Tri des problèmes** par difficulté croissante (selon le temps d’exécution de A*).
3. **Génération de visualisations** :
    - Comparaison des temps d’exécution (runtime).
    - Comparaison des makespans (longueur des plans).
    - Tableau récapitulatif avec statistiques globales.

#### Format CSV

```csv
Algorithm,Plan_Length,Time_Seconds,Nodes_Simulations,Success
A*,44,397.399,612355,1
Monte_Carlo,0,300.004,44592,0
```

---

## Compilation et Exécution

### Prérequis

- **Java** : JDK 11 ou supérieur
- **Python** : 3.7+ avec matplotlib, pandas, numpy
- **PDDL4J** : Bibliothèque dans `lib/`

### Installation des dépendances Python

```bash
pip install matplotlib pandas numpy
```

### Compilation

#### Sur Windows (PowerShell)

```powershell
# Compiler le planificateur
javac -cp "lib/*" -d classes src/fr/uga/pddl4j/examples/asp/*.java
```

#### Sur Linux/Mac

```bash
# Compiler le planificateur
javac -cp "lib/*" -d classes src/fr/uga/pddl4j/examples/asp/*.java
```

### Exécution

#### Tester un problème spécifique

**Windows (PowerShell) :**
```powershell
java -cp "classes;lib/*" fr.uga.pddl4j.examples.asp.ASP src/fr/uga/pddl4j/examples/asp/blocks/domain-b.pddl src/fr/uga/pddl4j/examples/asp/blocks/p001.pddl -t 300
```

**Linux/Mac :**
```bash
java -cp "classes:lib/*" fr.uga.pddl4j.examples.asp.ASP src/fr/uga/pddl4j/examples/asp/blocks/domain-b.pddl src/fr/uga/pddl4j/examples/asp/blocks/p001.pddl -t 300
```

**Options :**
- `-t` : Timeout en secondes (défaut: 600)

#### Générer les graphiques

```bash
# Après exécution du planificateur ASP.java (qui génère comparison_results.csv)
python compare_algorithms.py
```

---

## Résultats et Analyse

Cette section présente les résultats expérimentaux obtenus pour chacun des domaines, ainsi qu’une analyse comparative des performances des deuxF algorithmes.

### Synthèse Globale

Les expérimentations ont été menées sur 10 instances par domaine. Voici les observations des quatre domaines (Blocksworld, Depots, Freecells, Logistics) :

#### Nombre de Problèmes Résolus
Ici on ne parlera pas de "taux de réussite" mais du **nombre de problèmes résolus par chaque algorithme** car nous n'avons pas pu utiliser *VAL* pour valider les plans. Ces derniers sont trop volumineux et engendre des erreurs avec le validateur.

| Domaine         | A*                 | Monte Carlo        |
| --------------- | ------------------ | ------------------ |
| **Blocksworld** | 10 / 10 (100 %)    | 10 / 10 (100 %)    |
| **Depots**      | 8 / 10 (80 %)      | 2 / 10 (20 %)      |
| **Freecells**   | 10 / 10 (100 %)    | 10 / 10 (100 %)*   |
| **Logistics**   | 10 / 10 (100 %)    | 6 / 10 (60 %)      |
| **Total**       | **38 / 40 (95 %)** | **28 / 40 (70 %)** |

*Pour Freecells, Monte Carlo atteint le timeout sur toutes les instances, mais certaines solutions sont trouvées juste avant la limite.

**Analyse :**
- A* résout quasi-totalement l’ensemble des problèmes.
- Monté Carlo échoue dans 30 % des cas, principalement à cause :
    - Du dépassement du timeout.
    - De l’exploration aléatoire inefficace dans les grands espaces d’états.

#### Performance du temps d'exécution (Runtime)

Les tableaux ci-dessous présentent les temps d’exécution moyens (en secondes) par domaine.

| Domaine         | A* (moyenne) | MC (moyenne) | Observation principale                                        | Gagnant |
| --------------- | ------------ | ------------ | ------------------------------------------------------------- | ------- |
| **Blocksworld** | 0,11 s       | 34,34 s      | MC trouve des solutions mais en un temps très élevé           | **A***  |
| **Depots**      | 122,06 s     | 186,96 s     | MC atteint fréquemment le timeout (300 s)                     | **A***  |
| **Freecells**   | 0,53 s       | 300,01 s     | MC atteint systématiquement le timeout                        | **A***  |
| **Logistics**   | 0,72 s       | 56,47 s      | MC trouve parfois une solution mais reste très lent           | **A***  |


**Analyse :**
- A* est systématiquement plus rapide que Monte Carlo sur l’ensemble des domaines.
- Monte Carlo atteint souvent le timeout de 300 secondes, en particulier sur Depots et Freecells.
- Lorsqu’une solution est trouvée, MC reste plusieurs ordres de grandeur plus lent que A*.
- Le gain de performance d’A* est particulièrement marqué sur Blocksworld (facteur supérieur à ×300 sur certaines instances).

#### Qualité des Plans (*Makespan*)

Le *makespan* correspond au nombre d’actions dans le plan. Une valeur de `0` indique qu’aucune solution n’a été trouvée.

| Domaine         | A* (moyenne) | MC (moyenne) | Observation                                       | Gagnant |
| --------------- | ------------ | ------------ | ------------------------------------------------- | ------- |
| **Blocksworld** | 12,2         | 22,2         | MC produit des plans presque deux fois plus longs | **A***  |
| **Depots**      | 21,8         | 4,7          | MC échoue sur la majorité des instances           | **A***  |
| **Freecells**   | 11,1         | 13,7         | Plans plus longs pour MC                          | **A***  |
| **Logistics**   | 19,5         | 27,3         | Plans plus longs pour MC                          | **A***  |

**Analyse :**
- A* génère des plans plus courts et plus réguliers.
- Lorsque MC réussit, les plans produits sont systématiquement plus longs que ceux d'A*.
- Les valeurs faibles du makespan moyen pour MC sur Depots sont dues au fait que la majorité des instances ne sont pas résolues (7 plans sur 10 ont une valeur à 0, cela est dû au timeout).

### Analyse par Domaine

#### 1. Blocksworld

**Caractéristiques :**
Le domaine est relativement simple, avec un espace d’états modéré et des actions bien structurées.

**Résultats :**
- **A*** : Temps extrêmement faibles (< 0,1 s en moyenne) et plans courts.
- **MC** : Toutes les instances sont résolues, mais avec des temps allant jusqu’à ~95 s et des plans plus longs.
- **Conclusion** : A* est largement supérieur, Monté Carlo reste fonctionnel mais moins performant.

#### 2. Depots

**Caractéristiques :**
Le domaine complexe combinant transport et stockage, avec un facteur de branchement élevé.

**Résultats :**
- **A*** : Résout la majorité des problèmes, certains proches du timeout.
- **MC** : Échec sur 8 instances sur 10, souvent par timeout.
- **Conclusion** : Monté Carlo est inadapté à ce domaine (Il pourrait être intéressant d'ajouter une heuristique par exemple).

#### 3. Freecells

**Caractéristiques :**
Se base sur le jeu de cartes "Freecell", il y a un très grand espace d’états et nombreuses contraintes.

**Résultats :**
- **A*** : Tous les problèmes sont résolus rapidement (< 1,3 s).
- **MC** : Toutes les exécutions atteignent le timeout (~300 s), certaines solutions étant trouvées uniquement très tard, juste avant la limite de temps.
- **Conclusion** : A* est nettement plus efficace et fiable.

#### 4. Logistics

**Caractéristiques :**
Ce sont des problèmes de planification multi-niveaux avec des solutions profondes.

**Résultats :**
- **A*** : Résolution complète avec des temps inférieurs à 1 s en moyenne.
- **MC** : Échec sur 4 instances, même sans atteindre le timeout (aucune solution trouvée).
- **Conclusion** : A* est plus robuste et plus fiable que Monté Carlo.

### Conclusion Générale
Les résultats expérimentaux montrent de manière très nette la supériorité du planificateur A* sur l’approche Monté Carlo basée sur random walks, tant en termes de temps d’exécution que de qualité et de fiabilité des plans produits. Si l’algorithme Monté Carlo parvient à résoudre certains problèmes simples, il devient rapidement inefficace dès que l’espace d’états ou le facteur de branchement augmente, conduisant à de nombreux timeouts ou à l’absence totale de solution. Ces limitations s’expliquent par l’absence de toute heuristique, contrairement à A* qui oriente efficacement la recherche. Ainsi, cette étude met en évidence que le Monté Carlo constitue avant tout une base de recherche, mais qu’il reste inadapté aux problèmes de planification classiques de taille réaliste sans l’ajout de mécanismes de sélection, d’heuristiques ou d’apprentissage.

---

## Annexe : Résultats détaillés par domaine

### Blocksworld
**Temps d’exécution (en secondes) :**
| Algorithme | P001  | P002  | P003  | P004   | P005   | P006   | P007   | P008   | P009   | P010   | Moyenne     |
| ---------- | ----- | ----- | ----- | ------ | ------ | ------ | ------ | ------ | ------ | ------ | ----------- |
| **A***     | 0,034 | 0,016 | 0,015 | 0,041  | 0,038  | 0,041  | 0,069  | 0,062  | 0,37   | 0,439  | **0,1125**  |
| **MC**     | 2,178 | 2,362 | 1,904 | 23,261 | 26,653 | 37,458 | 50,946 | 58,429 | 45,732 | 94,493 | **34,3416** |

**Longueur des plans (Makespan) :**
| Algorithme | P001 | P002 | P003 | P004 | P005 | P006 | P007 | P008 | P009 | P010 | Moyenne  |
| ---------- | ---- | ---- | ---- | ---- | ---- | ---- | ---- | ---- | ---- | ---- | -------- |
| **A***     | 6    | 10   | 6    | 12   | 10   | 12   | 16   | 10   | 20   | 20   | **12,2** |
| **MC**     | 6    | 10   | 6    | 14   | 12   | 12   | 22   | 18   | 62   | 60   | **22,2** |

### Depots
**Temps d’exécution (en secondes) :**
| Algorithme | P001   | P002    | P003   | P004   | P005    | P006    | P007    | P008    | P009    | P010    | Moyenne      |
| ---------- | ------ | ------- | ------ | ------ | ------- | ------- | ------- | ------- | ------- | ------- | ------------ |
| **A***     | 0,053  | 0,147   | 3,114  | 14,197 | 397,399 | 148,997 | 1,381   | 276,554 | 373,382 | 5,346   | **122,057**  |
| **MC**     | 33,877 | 136,548 | 28,645 | 38,844 | 300,004 | 131,699 | 300,001 | 300,003 | 300,02  | 300,004 | **186,9645** |

**Longueur des plans (Makespan) :**
| Algorithme | P001 | P002 | P003 | P004 | P005 | P006 | P007 | P008 | P009 | P010 | Moyenne  |
| ---------- | ---- | ---- | ---- | ---- | ---- | ---- | ---- | ---- | ---- | ---- | -------- |
| **A***     | 10   | 25   | 29   | 30   | 44   | 0    | 21   | 34   | 0    | 25   | **21,8** |
| **MC**     | 11   | 36   | 0    | 0    | 0    | 0    | 0    | 0    | 0    | 0    | **4,7**  |

### Freecells
**Temps d’exécution (en secondes) :**
| Algorithme | P001    | P002    | P003    | P004    | P005   | P006    | P007    | P008    | P009    | P010    | Moyenne      |
| ---------- | ------- | ------- | ------- | ------- | ------ | ------- | ------- | ------- | ------- | ------- | ------------ |
| **A***     | 0,189   | 0,143   | 0,172   | 0,232   | 0,551  | 0,906   | 0,696   | 0,289   | 0,844   | 1,246   | **0,5268**   |
| **MC**     | 300,002 | 300,006 | 300,005 | 300,004 | 300,01 | 300,003 | 300,008 | 300,003 | 300,019 | 300,004 | **300,0064** |

**Longueur des plans (Makespan) :**
| Algorithme | P001 | P002 | P003 | P004 | P005 | P006 | P007 | P008 | P009 | P010 | Moyenne  |
| ---------- | ---- | ---- | ---- | ---- | ---- | ---- | ---- | ---- | ---- | ---- | -------- |
| **A***     | 9    | 8    | 8    | 8    | 9    | 15   | 14   | 13   | 13   | 14   | **11,1** |
| **MC**     | 9    | 8    | 9    | 8    | 9    | 20   | 17   | 20   | 16   | 21   | **13,7** |

### Logistics
**Temps d’exécution (en secondes) :**
| Algorithme | P001   | P002   | P003   | P004  | P005   | P006   | P007   | P008   | P009   | P010   | Moyenne     |
| ---------- | ------ | ------ | ------ | ----- | ------ | ------ | ------ | ------ | ------ | ------ | ----------- |
| **A***     | 0,416  | 0,618  | 0,201  | 1,631 | 0,391  | 0,063  | 2,095  | 0,154  | 0,786  | 0,8    | **0,7155**  |
| **MC**     | 72,146 | 48,597 | 48,846 | 59,98 | 48,963 | 46,141 | 62,486 | 58,625 | 61,334 | 57,546 | **56,4664** |

**Longueur des plans (Makespan) :**
| Algorithme | P001 | P002 | P003 | P004 | P005 | P006 | P007 | P008 | P009 | P010 | Moyenne  |
| ---------- | ---- | ---- | ---- | ---- | ---- | ---- | ---- | ---- | ---- | ---- | -------- |
| **A***     | 20   | 19   | 15   | 27   | 17   | 8    | 26   | 14   | 25   | 24   | **19,5** |
| **MC**     | 61   | 61   | 54   | 0    | 41   | 9    | 0    | 47   | 0    | 0    | **27,3** |

---

## Contributeurs

- ARBAUT Jean-Baptiste
- TROPEL Célia

**Université :** Université Grenoble Alpes

---

*Projet réalisé dans le cadre du cours de Planification Automatique - Master 2 MIASHS Informatique et Cognition - 2025/2026*

