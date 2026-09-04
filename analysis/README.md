# Jupyter statistical analysis

The `cowboy_analysis.ipynb` notebook in this directory does not reimplement the
game engine. It runs the Java JAR in batch mode and analyzes the resulting CSV
summaries with Python.

## Setup

```bash
pip install -r analysis/requirements.txt
jupyter lab
```

Open the notebook from either the project root or this `analysis` directory and
run the cells in order.

## Experiments

`FIXED_STARTER = None` lets the Java engine choose a random starter in every
game. This experiment checks the expected uniform distribution across absolute
cowboy IDs.

`FIXED_STARTER = 0` keeps the starter fixed and studies the winner's offset from
that starter. The cowboys face the center of the circle, and IDs increase in
each cowboy's own `LEFT` direction in the Java representation. Offset `+1` is
the starter's first `LEFT` neighbor, while offset `N-1` is the first `RIGHT`
neighbor.

Normalized offset is calculated as `offset / N`. For example, `0.70` means 70%
of the circle toward `LEFT`; the same position is 30% of the circle toward
`RIGHT`. Because the first shot always goes `RIGHT`, this distribution is not
expected to be uniform.

The notebook compares results for N=2, 3, 5, 10, 20, 50, 100, and 200. It uses
`fixed-starter-n*.csv` for the scale analysis and `random-starter-n5.csv` for the
random-starter control.

The analysis includes chi-square goodness-of-fit, Cohen's w, Pearson residuals,
95% Wilson confidence intervals, game length, winner-performance correlations,
and sample-size precision. A small p-value alone does not establish practical
importance; effect size, confidence intervals, and observed probability gaps
must be interpreted together.

In JupyterLite, uploading the notebook and `fixed-starter-n5.csv` to the same
browser directory is enough for the main analysis. Comparison sections run only
when their corresponding additional CSV files are also available.
