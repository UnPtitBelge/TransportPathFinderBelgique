
# 🚆 Algo-Project

**Recherche de plus courts chemins dans les réseaux de transport en commun**
Projet Java utilisant les algorithmes A* et Dijkstra, avec profils d’utilisateur, benchmarks et analyse de performance.

---

## 📦 Installation

1. **Cloner le dépôt**
   ```sh
   git clone https://github.com/UnPtitBelge/Algorithmique-2-Projet.git
   cd Algo-Project
   ```

2. **Prérequis**
   - Java 23+
   - Maven 3.8+
   - (Optionnel) `algs4.jar` dans `libs/`

3. **Compilation**
   ```sh
   mvn clean compile
   ```

---

## 🚀 Exécution

Lancement du programme :

````sh
  mvn exec:java@run-transport_path_finder -Dexec.args="--dijkstra --speed --walk-speed --profile --custom-profile"
````

Lancement des différents benchmarks et tests :

- **Benchmark de profils**
  ```sh
  mvn exec:java@run-profile_benchmark -Dexec.args="--dijkstra --random --csv --static chemin.csv"
  ```

- **Benchmark de vitesse**
  ```sh
  mvn exec:java@run-speed_benchmark -Dexec.args="--random --csv"
  ```

- **Analyse**
  ```sh
  mvn exec:java@run-analysis -Dexec.args="--dijkstra --random --csv"
  ```

- **Tests heuristique admissible/consistante**
  ```sh
  mvn exec:java@run-heuristic_tests
  ```

**Paramètres disponibles** :
- `--dijkstra` / `-d` : active le mode Dijkstra (désactive l’heuristique)
- `--random` / `-r` : mode random (au lieu de statique)
- `--static` / `-s` : force le mode statique
- `--csv` / `-c` : chemin du fichier CSV de sortie
- `--speed` : change la vitesse de l'heuristique
- `--walk-speed` : change la vitesse de la marche
- `--profile` : change de profil [NONE, COMFORTABLE, WALKING_FRIENDLY, FAST]
- `custom-profile`: ajoute un profil personnalisé.

---

## 🧩 Structure du projet

```
src/
 ├── main/
 │    └── java/astar/theorically/
 │         ├── Algorithm/    # Astar, Dijkstra, etc.
 │         ├── Models/       # Stop, Trip, PathEdge...
 │         ├── Parser/       # Chargement des données GTFS
 │         ├── Utils/        # Heuristique,
           ├── TansportPathFinder.java
 Profils, Helpers
 └── test/
      └── java/astar_benchmark/
           ├── ProfileBenchmark.java
           ├── SpeedBenchmark.java
           ├── Analysis.java
           └── HeuristicTests.java
```

---

## 📊 Fonctionnalités

- **A*** et **Dijkstra** pour la recherche de chemin
- Profils utilisateurs personnalisables (`NONE`, `COMFORTABLE`, `FAST`, etc.)
- Benchmarks reproductibles (statique) ou aléatoires (random)
- Export CSV des résultats pour analyse
- Tests d’admissibilité et de consistance de l’heuristique

---

## 📝 Exemples de CSV générés

- `analysis_static.csv` : distance, temps de trajet, noeuds explorés
- `speed_benchmark_static.csv` : vitesse, distance, temps, stops étendus

---

## 👨‍💻 Auteurs

- Matteo Morbée

---

## 📄 Licence

Projet académique – usage pédagogique uniquement.

---
