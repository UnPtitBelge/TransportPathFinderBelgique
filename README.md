
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

3. **Compilation**
  Si vous compilez pour la première fois, utilisez :
   ```sh
   mvn clean install compile
   ```
  Sinon vous pouvez simplement compiler le projet :
   ```sh
   mvn compile
   ```
  Faites 'clean' si vous avez des erreurs de compilation

---

## 🚀 Exécution

Lancement du programme :

````sh
  mvn exec:java@run-path_finder -Dexec.args="--dijkstra --speed --walk-speed --profile --custom-profile"
````

Lancement des différents benchmarks et tests :

- **Benchmark de profils**
  ```sh
  mvn exec:java@run-benchmark-profile -Dexec.args="--dijkstra --random --csv --static chemin.csv"
  ```

- **Benchmark de vitesse**
  ```sh
  mvn exec:java@run-benchmark-speed -Dexec.args="--random --csv"
  ```

- **Analyse**
  ```sh
  mvn exec:java@run-benchmark-analysis -Dexec.args="--dijkstra --random --csv"
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

```Algo-Project/
├── README.md
└── src/
    └── main/
        └── java/
            ├── Algorithm/
            │   └── Astar.java
            ├── Benchmark/
            │   ├── Analysis.java
            │   ├── HeuristicTests.java
            │   ├── ProfileBenchmark.java
            │   └── SpeedBenchmark.java
            ├── Models/
            │   ├── Neighbour.java
            │   ├── PathEdge.java
            │   ├── Route.java
            │   ├── Stop.java
            │   └── Trip.java
            ├── Parser/
            │   ├── CsvLoader.java
            │   ├── CsvProcessor.java
            │   ├── Parser.java
            │   ├── RouteParser.java
            │   ├── StopParser.java
            │   ├── StoptimeParser.java
            │   └── TripParser.java
            ├── Utils/
            │   ├── Helper.java
            │   ├── Heuristic.java
            │   └── Profiles.java
            ├── algs4/
            │   ├── IndexMinPQ.java
            │   └── StdOut.java
            └── TransportPathFinder.java
 ```

---

## 📊 Fonctionnalités

- **A*** et **Dijkstra** pour la recherche de chemin
- Profils utilisateurs personnalisables (`NONE`, `COMFORTABLE`, `FAST`, etc.)
- Benchmarks reproductibles (10 voyages identiques pour chaque benchmark) ou aléatoires (random)
- Export CSV des résultats pour analyse (par défaut dans src/main/resources/benchmark/)
- Tests d’admissibilité et de consistance de l’heuristique (dans le terminal)

---

## 📝 Exemples de CSV générés

- `analysis.csv` : voyage, distance, temps de trajet, noeuds explorés
- `speed.csv` : vitesse, distance, temps, stops étendus

---

## 👨‍💻 Auteurs

- Matteo Morbée

---

## 📄 Licence

Projet académique – usage pédagogique uniquement.

---
