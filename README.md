# MWTek Cowboy Shootout

A reproducible Wild West shootout simulation built with Java 21 and Maven, with a JSON protocol for every game.

## If this is your first time reading the code

Read only these three places first to understand how one shot works:

1. `ShootoutGame.playNextShot`: shows the shooter, direction, target, and damage flow.
2. `ShootoutState`: manages the current cowboy and the circle of living cowboys.
3. `Cowboy`: stores one cowboy's HP and the IDs of the living neighbors on the left and right.

The single-shot flow across these files is:

```text
current cowboy
  → choose RIGHT when HP is even, LEFT when HP is odd
  → select the direct living neighbor as the target
  → apply 1–5 damage
  → if the target dies, remove it from the circle and keep the shooter active
  → if the target survives, make the target the new current cowboy
```

`DamageGenerator` and `GameRules` explain only the details of damage generation.
`ShotEvent` and `ShootoutResult` describe the immutable output produced after the
game is played. You do not need to open the CLI, batch, statistics, or output
classes to understand the single-shot rule.

## Quick start

```bash
mvn test
mvn package
java -jar target/cowboy-shootout-1.0.0.jar
java -jar target/cowboy-shootout-1.0.0.jar 5 --seed 123456 \
  --output shootout-protocols/cowboy-shootout-protocol.json
```

When the JAR or IntelliJ `Main` is started without arguments, the program asks
for the number of cowboys. It accepts an integer from `1` through `1,000,000`;
pressing Enter uses `5`. The normal prompt does not display the upper limit.
Negative values, zero, text, and values above the limit are rejected with a
clear message, and the program asks again. When the first program argument is
provided, the program uses that number without displaying the prompt.

The `--seed` option reproduces the same starting cowboy and damage rolls. When
no seed is provided, the application generates one and records the actual value
in both the console and JSON protocol, so every detailed game can be replayed.
For a command-line game without `--output`, the default path is
`shootout-protocols/cowboy-shootout-protocol.json`. The source code contains no
user-specific absolute path.

Interactive mode asks for a new cowboy count after every completed game. Enter
a number to start another game or `Q` to close the application. Every
interactive game is archived in the `shootout-protocols` directory under the
current working directory as
`cowboy-shootout-protocol-seed-<seed>.json`. Existing game archives are not
overwritten. Command-line single-game and batch runs execute once and exit
without waiting, which keeps scripts and automation usable.

The final console output also reports the winner's position relative to the
starter. For example, `offset +2/5` and `40.0% toward LEFT` mean that the winner
is two positions from the starter in the direction of increasing cowboy IDs.
The same position is `60.0% toward RIGHT`. Because the cowboys face the center
of the circle, `LEFT` and `RIGHT` are each cowboy's own directions rather than
the screen directions of an outside observer. This is the same convention used
by `winnerOffsetFromStarter` in the batch CSV and by the relative-position
analysis in the notebook.

## Jupyter analysis

`analysis/cowboy_analysis.ipynb` uses the Java engine to generate batch CSV data
and analyzes it with pandas, SciPy, and Matplotlib. Open the notebook in Jupyter
and run its cells in order. Required Python packages are listed in
`analysis/requirements.txt`.

```bash
java -jar target/cowboy-shootout-1.0.0.jar 5 --batch 10000 --starter 0 --seed 123456 \
  --summary analysis/output/fixed-starter-n5.csv
```

Providing `--starter` measures relative-position bias. Omitting it randomizes
the starter and measures fairness between absolute cowboy IDs. Batch output is
not shot-level data: it contains one summary CSV row per game.

Each row also includes the winner's shots fired, kills, and total effective
damage. Because the batch engine returns a `ShootoutSummary`, it does not create
or retain detailed `ShotEvent` history during this analysis.

## Structure at a glance

1. `ShootoutState` stores HP, left and right neighbors, the current cowboy, and the number of living cowboys.
2. `ShootoutGame` runs the game flow by selecting the direction and target.
3. `Cowboy` stores its own HP and the IDs of its living left and right neighbors.
4. `DamageGenerator` produces damage from 1 through 5 through an injected `RandomSource`.
5. Every detailed-game shot is recorded as an immutable `ShotEvent` that retains no mutable state reference.
6. Batch mode summarizes the same game with `ShootoutSummary` without creating history and copies only the winner counters needed in the result.
7. `ProtocolWriter` writes UTF-8 JSON; after the file is closed, `ChecksumCalculator` streams its actual bytes and calculates SHA-256.

The live game state (`Cowboy[]`, current cowboy ID, and remaining count), the
immutable `ShotEvent` history, and statistics counters are separate
responsibilities. At the end of a detailed game, the JSON protocol's
`statistics` field contains each cowboy's shots fired, kills, effective damage
dealt and received, and hits received.

```text
src/main/java/com/mwtek/shootout/
  app/Main.java                 application entry point
  cli/                          typed commands, parser, and usage text
  game/
    Cowboy.java                 one cowboy's HP and neighbor IDs
    ShootoutState.java          Cowboy[] circle, current ID, and remaining count
    ShootoutGame.java           game flow
    Direction.java              LEFT / RIGHT domain vocabulary
    result/                     events, winners, detailed and compact results
    statistics/                 mutable counters and immutable snapshots
    random/                     random-source abstraction and implementation
    ...                         rules, setup, damage, and operational limits
  batch/                        repeated simulations and CSV output
  output/                       console, JSON protocol, and SHA-256
src/test/java/...               deterministic unit and pipeline tests
```

## Design decisions

`ShootoutState` owns a `Cowboy[]` indexed by permanent cowboy ID. While Cowboy
2 is alive, `cowboys[2]` contains its object. After elimination, the same slot
becomes `null`; no other cowboy moves into it and the array is not compacted.
Every living `Cowboy` stores its HP and the IDs of its living right and left
neighbors.

At initialization, for cowboys facing the center, the right neighbor is
`id - 1 (mod N)` and the left neighbor is `id + 1 (mod N)`. When a cowboy is
eliminated, its two living neighbors are linked to each other and the eliminated
cowboy's permanent-ID slot is cleared. Target lookup and elimination remain
O(1).

For five cowboys, the ID direction is:

```text
LEFT:   0 → 1 → 2 → 3 → 4 → 0
RIGHT:  0 → 4 → 3 → 2 → 1 → 0

Example: Cowboy 2's LEFT neighbor is Cowboy 3.
         Cowboy 2's RIGHT neighbor is Cowboy 1.
```

Damage values from 1 through 5 are equally likely. Lethal damage clamps HP to
zero, while `damageRolled`, `effectiveHpLost`, and `overkill` remain separate
JSON fields. A surviving target becomes the active cowboy. If the target dies,
the shooter fires again within the same turn. The game ends immediately when
one cowboy remains, so no self-shot occurs.

Names inside the Java domain remain explicit: `shooterCowboyId`,
`targetCowboyId`, `shooterHealthPoints`, and `effectiveHealthPointsLost`.
At the JSON boundary these are written as `shooterId`, `targetId`, `shooterHp`,
and `effectiveHpLost` to preserve the established protocol field names.

`shotNumber` increases after every shot. `activeCowboyTurnNumber` increases only
when a surviving target becomes the new active cowboy. When a cowboy kills its
target and fires again, two different shots can share the same active-cowboy
turn number. The JSON name remains `turnNumber` for protocol compatibility.

## Fairness summary

Before the starter is selected, every named cowboy has a `1/N` win probability
because the system is rotationally symmetric. Relative positions are not
equally likely to win once the starter is fixed: initial HP is 10, so the first
shot always goes to the right neighbor. The notebook measures this distinction
with separate fixed-starter and random-starter experiments.

In the five-cowboy experiment, offset `+2`, which is `40%` toward `LEFT`, has the
highest observed win rate at approximately `27.4%`. In the random-starter
experiment, permanent cowboy IDs remain near `1/N`. Results and larger-circle
comparisons are in `analysis/cowboy_analysis.ipynb`.

## Architecture

| Component | Responsibility |
| --- | --- |
| `Cowboy` | One cowboy's ID, HP, living left/right neighbor IDs, and alive state |
| `ShootoutState` | The `Cowboy[]` circle, current cowboy ID, and remaining count |
| `ShootoutGame` | Turn, direction, target, damage, elimination, and completion flow |
| `DamageGenerator` | Produces damage from 1 through 5 through the shared random source |
| `ShotEvent` | Immutable audit snapshot of one shot |
| `ShootoutSummary` | Compact batch result without detailed history |
| `StatisticsCollector` | Updates shot, kill, and effective-damage counters in O(1) |
| `ProtocolWriter` / `ChecksumCalculator` | Writes UTF-8 JSON and hashes the actual file bytes with SHA-256 |
| `BatchSimulationWriter` | Writes one CSV row per game produced by the Java engine |

`ShotEvent` does not retain references to arrays inside mutable `ShootoutState`,
so an old event's HP values cannot change as the game continues.

The normal game flow can be followed directly as:

```text
Main.main
  → CliParser.parse
  → ShootoutGame.play
  → ShootoutGame.playNextShot
  → ShootoutState.applyDamageToCowboy
  → Cowboy.takeDamage
  → if the target survives: passTurnToCowboy
  → if the target dies: eliminateCowboy and reconnect the neighbors
  → ShootoutResult
  → ConsoleReporter + ProtocolWriter + ChecksumCalculator
```

The batch flow calls `ShootoutGame.summarize`. Because it produces a
`ShootoutSummary`, it never creates the detailed event list used by normal games.

```text
com.mwtek.shootout
├── app/                 application composition root (`Main`)
├── cli/                 command-line parsing, commands, and usage
├── batch/               repeated simulations and CSV summaries
├── game/                Cowboy, state, rules, setup, and game flow
│   ├── random/          seeded random-value sources
│   ├── result/          immutable events, winners, and completed-game results
│   └── statistics/      in-game counters and immutable statistics
└── output/              terminal, JSON protocol, and checksum
```

The classes that run the shootout remain together in `game`. Supporting output
values, statistics, and randomness have dedicated subpackages so a reader can
find them by responsibility. The `output` package knows nothing about game
decisions, `batch` does not request detailed history, and `app` connects the
dependencies.

## Circle representation and complexity

A cowboy ID is also its permanent `Cowboy[]` index and never changes during a
game. When a cowboy dies, its left and right neighbor IDs are read and those two
living `Cowboy` objects are reconnected. The dead cowboy's slot is then set to
`null`, allowing its object to be garbage-collected. Surviving IDs and indexes
do not change. The array retains its initial length, so peak memory complexity
remains O(N).

| Operation | Cost |
| --- | --- |
| Initialization | O(N) |
| Left/right target lookup | O(1) |
| HP update | O(1) |
| Remove a dead target from the circle | O(1) |
| Retrieve the winner | O(1) |
| Complete game | O(N) |

Initial total HP is `10N`. Every shot reduces total HP by at least 1, and the
winner has at least 1 HP remaining. A game therefore ends after at most
`10N - 1` shots. Detailed-mode event history also uses at most O(N) space.

## Rules and edge cases

- Direction depends only on the **shooter's HP before the shot**: even means `RIGHT`, odd means `LEFT`.
- A surviving target becomes the active cowboy. If the target dies, the same shooter fires again.
- `N=1`: Cowboy 0 wins with no shots; JSON and a checksum are still produced.
- `N=2`: the left and right neighbor can be the same cowboy; the direction rule still applies.
- Lethal damage clamps HP to `0`. The rolled value is `damageRolled`, applied damage is `effectiveHpLost`, and the excess is `overkill`.
- The game ends immediately after the final elimination; the survivor never shoots itself.

## Protocol and integrity

Jackson writes the JSON protocol with UTF-8. In addition to the required fields
for every shot, it includes the starter, seed, winner, and per-cowboy statistics.
SHA-256 is calculated from the actual file bytes only after the file is closed.

JSON and batch CSV output are first completed in a temporary file inside the
destination directory and then moved into place. If writing fails midway, a
previous valid destination remains intact. If the operating system does not
support an atomic move, the implementation falls back to a normal replacement
move while retaining the safe temporary-file approach. Concurrent runs writing
the same destination never interleave their contents; the last completed run
owns the destination.

The checksum calculation does not load the full file into memory. It streams
the file through a fixed-size buffer.

SHA-256 is an integrity fingerprint. It detects whether a protocol differs from
the file that produced a trusted checksum, but it is not a signature or HMAC and
cannot authenticate against an attacker who can replace both the file and hash.

## Test strategy

JUnit tests cover interactive cowboy-count selection, retries after invalid
input, circular links, clearing eliminated cowboy slots, controlled access to a
dead ID, direction selection, surviving and killed targets, overkill clamping,
self-shot prevention, seed reproducibility, immutable history, parseable UTF-8
JSON with required shot fields, a known SHA-256 value, detection of actual file
changes, batch CSV output, and the `N=1000` termination bound.

```bash
mvn test
```

## Code and exception rules

- Shared constants are `static final`; fields and local values that are not reassigned are `final`. Game state, counters, and loop variables are intentionally mutable.
- An `if` is used only for a clear decision such as input validation, a state invariant, or a genuine game branch. A simple domain decision is not distributed across Strategy classes merely to reduce the number of conditions.
- Invalid user or API input produces `IllegalArgumentException`, an impossible game state produces `IllegalStateException`, and file-system failures remain `IOException`.
- Exceptions are caught only when the application can recover or add meaningful context. Checkstyle forbids catching `Throwable` or `Error` and forbids empty `catch` blocks.
- Files and streams are closed with try-with-resources. When parsing exceptions are translated, the original cause is preserved.
- One-off, context-specific validation messages remain at their call sites; repeated console and format text uses named constants.

## Theory and analysis

Before the starter is randomized, the system has rotational symmetry, so every
named cowboy has `P(win) = 1/N`. Once the starter is fixed, the relative-offset
distribution need not be uniform; with initial HP equal to 10, the first shot
always goes `RIGHT`.

`analysis/cowboy_analysis.ipynb` investigates these questions with separate
experiments. Monte Carlo output does not replace the symmetry proof; it checks
whether the Java implementation behaves consistently with that theoretical
expectation. See [analysis/README.md](analysis/README.md) for details.

## Assumptions and limits

- Damage values from 1 through 5 are equally likely.
- The starter is uniformly random; `--starter` fixes it only in batch positional-analysis mode.
- Physical clockwise direction is not defined. `RIGHT` and `LEFT` are domain names for arena neighbors.
- Python and Jupyter are not Java runtime dependencies.
- Detailed JSON/protocol mode accepts at most 10,000 cowboys because it retains
  every shot, prints every shot, and archives the full history. In interactive
  mode, larger counts automatically run 10 compact simulations and write a CSV
  summary instead of creating an impractically large shot-by-shot JSON file.
- Batch mode accepts at most 1,000,000 cowboys and 1,000,000 simulations.
  The product of cowboy count and simulation count must remain at most 10,000,000;
  therefore, a one-million-cowboy batch can contain at most 10 simulations.
- In batch mode, `cowboy count × simulation count` cannot exceed 10,000,000. Validation uses `long`, so the multiplication itself cannot overflow.
- These limits reject unexpected heap, CPU, disk, and IntelliJ console consumption before work starts. Invalid command-line input returns process exit code `2` and creates no destination file.
