# Simplified Path Finder Belgium

Calcul du plus court chemin en temps de trajet dans le réseau de transport en
commun belge (STIB, TEC, SNCB, DELIJN), à partir de données GTFS, via un A*
adapté aux horaires.

## Installation

Prérequis : Java 25+, Maven 3.8+.

```sh
mvn compile
```

## Exécution

```sh
mvn exec:java@run-main
```

Le point de départ, l'arrivée et l'heure de recherche sont actuellement fixés
dans [`Main.java`](src/main/java/com/ulb/Main.java).

Les données GTFS sont attendues dans `data/GTFS/<AGENCE>/` (`routes.csv`,
`stops.csv`, `trips.csv`, `stop_times.csv`), une fois par agence listée dans
[`Agency`](src/main/java/com/ulb/model/Agency.java).

## L'algorithme

Le graphe n'est pas construit explicitement : les arêtes sont dérivées à la
volée pendant la recherche, à partir de deux sources locales à chaque arrêt :

- **Arêtes "trajet"** : monter dans un véhicule à un arrêt et descendre au
  suivant du même trajet (`Timetable.stopTimesAt` + `Timetable.nextStopTime`).
- **Arêtes "marche à pied"** : correspondance vers un arrêt physiquement
  proche mais avec un `stop_id` différent — utile car chaque agence GTFS a
  son propre référentiel d'arrêts, donc deux arrêts au même carrefour
  n'ont jamais le même identifiant (`Timetable.transfersAt`).

La recherche (`Astar.search`) est un A* classique : coût cumulé = temps
d'attente + temps de trajet/marche réellement écoulés depuis le départ,
heuristique = temps de trajet minimal à vol d'oiseau à 130 km/h
(`AStarHeuristic`, admissible tant qu'aucun véhicule ne dépasse cette
vitesse).

## Choix de conception

- **`Timetable`** regroupe arrêts, trajets, horaires et correspondances
  derrière une API unique (`findStopsByName`, `stopTimesAt`, `nextStopTime`,
  `transfersAt`), pour que l'algorithme n'ait pas à manipuler des `Map` GTFS
  brutes ni à connaître le format des fichiers sources.
- **Horaires triés par `stop_sequence`** à l'issue du parsing
  (`Parser.parseStopTimes`) : certains flux GTFS n'écrivent pas leurs lignes
  dans l'ordre de la séquence (ex. DELIJN), donc retrouver "l'arrêt suivant"
  d'un trajet ne peut pas reposer sur l'ordre du fichier.
- **`Astar.Frontier`** : la file de priorité ne garde qu'un seul candidat par
  arrêt à la fois (au lieu d'accumuler une entrée par relaxation), ce qui
  évite l'essentiel de la pression mémoire sur un réseau de plusieurs
  millions d'horaires.
- **Correspondances à pied précalculées une fois** (`WalkingTransferBuilder`)
  via une grille spatiale (taille de cellule = rayon de correspondance
  maximal, recherche dans le voisinage 3×3) plutôt qu'une comparaison de
  toutes les paires d'arrêts (67 000+ arrêts au total).
- **Regroupement à l'affichage par ligne, pas par trajet**
  (`SolutionFormatter.sameLine`) : un même numéro de ligne peut être
  découpé en plusieurs `route_id`/`trip_id` dans le GTFS source (ex. SNCB
  déclare un `route_id` distinct par trajet pour une même ligne "P") ; on
  fusionne donc les arrêts intermédiaires consécutifs d'une même ligne
  affichée plutôt que du même `trip_id` interne.
- **Logs structurés (SLF4J + Logback) et `Profiler`** (chrono + delta mémoire
  par phase, via `try (Profiler p = Profiler.start("phase")) { ... }`) pour
  identifier les goulots d'étranglement — le parsing des `stop_times.csv`
  domine largement le temps total face à la recherche elle-même.

## Structure du projet

```
src/main/java/com/ulb/
├── Main.java
├── astar/
│   ├── Astar.java            recherche A*, gestion de la frontière
│   ├── AStarHeuristic.java   heuristique (distance à vol d'oiseau / vitesse max)
│   ├── PriorityNode.java
│   ├── Solution.java         reconstruction du chemin depuis le noeud d'arrivée
│   ├── SolutionFormatter.java
│   └── StopNode.java
├── model/
│   ├── Agency.java
│   ├── Transport.java
│   └── gtfs/                 Stop, Trip, StopTime, Road, Transfer, Timetable
├── parser/
│   ├── GTFSLoader.java       lecture CSV (commons-csv)
│   ├── Parser.java
│   └── WalkingTransferBuilder.java
└── util/
    ├── Position.java         coordonnées + distance haversine
    ├── Profiler.java         chrono/mémoire par phase
    └── Utils.java            conversions de temps GTFS
```

## Licence

Projet académique — usage pédagogique uniquement.
