#!/usr/bin/env python3
"""
Comparaison A* (HSP) vs Monte Carlo (Pure Random Walk)
Métriques: Runtime et Makespan
"""

import matplotlib.pyplot as plt
import pandas as pd
import os

def load_data():
    if os.path.exists('comparison_results.csv'):
        df = pd.read_csv('comparison_results.csv')
        if 'Problem' not in df.columns:
            df['Problem'] = 'Problem_1'
        return df
    else:
        print("Erreur: Fichier comparison_results.csv non trouvé.")
        return None



def create_single_problem_charts(df):
    plt.style.use('seaborn-v0_8-whitegrid')
    fig, axes = plt.subplots(1, 2, figsize=(14, 6))
    
    colors = ['#2c3e50', '#e74c3c']
    
    ax1 = axes[0]
    bars1 = ax1.bar(df['Algorithm'], df['Time_Seconds'], color=colors,
                    edgecolor='black', linewidth=2, alpha=0.85)
    ax1.set_ylabel('Runtime (secondes)', fontsize=12, fontweight='bold')
    ax1.set_title('Métrique 1: Temps d\'Exécution', fontsize=14, fontweight='bold')
    ax1.grid(axis='y', alpha=0.3)
    
    for bar in bars1:
        height = bar.get_height()
        ax1.text(bar.get_x() + bar.get_width()/2., height * 1.05,
                f'{height:.3f}s', ha='center', va='bottom', fontweight='bold', fontsize=11)
    
    ax2 = axes[1]
    bars2 = ax2.bar(df['Algorithm'], df['Plan_Length'], color=colors,
                    edgecolor='black', linewidth=2, alpha=0.85)
    ax2.set_ylabel('Makespan (actions)', fontsize=12, fontweight='bold')
    ax2.set_title('Métrique 2: Longueur du Plan', fontsize=14, fontweight='bold')
    ax2.grid(axis='y', alpha=0.3)
    
    for bar in bars2:
        height = bar.get_height()
        ax2.text(bar.get_x() + bar.get_width()/2., height * 1.05,
                f'{int(height)}', ha='center', va='bottom', fontweight='bold', fontsize=11)
    
    plt.suptitle('Comparaison A* vs Monte Carlo', fontsize=16, fontweight='bold')
    plt.tight_layout()
    plt.show()

def main():
    print("="*70)
    print("COMPARAISON A* (HSP) vs MONTE CARLO")
    print("Métriques: Runtime et Makespan")
    print("="*70)
    
    print("\nChargement des données...")
    df = load_data()
    
    if df is None:
        return
    
    print("Données chargées")
    
    print("\nGénération des graphiques...")
    create_single_problem_charts(df)
    
    print("\n" + "="*70)
    print("Terminé")
    print("="*70)

if __name__ == "__main__":
    try:
        main()
    except Exception as e:
        print(f"\nErreur: {e}")
        import traceback
        traceback.print_exc()
