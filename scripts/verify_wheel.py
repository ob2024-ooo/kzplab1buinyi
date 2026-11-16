"""Встановлює wheel з dist/ у чисте середовище без dev-залежностей і запускає CLI."""

from __future__ import annotations

import subprocess
import sys
import venv
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


def main() -> int:
    """Повертає 0, якщо встановлений пакет створює очікуваний звіт."""
    # Явний UTF-8 для консолі: типове кодування Windows не знає українських літер.
    for stream in (sys.stdout, sys.stderr):
        stream.reconfigure(encoding="utf-8")
    wheels = sorted((ROOT / "dist").glob("*.whl"))
    if not wheels:
        print("У dist/ немає wheel: спершу виконайте python -m build", file=sys.stderr)
        return 1
    # Каталог середовища можна передати першим аргументом.
    env_dir = Path(sys.argv[1]) if len(sys.argv) > 1 else ROOT / ".venv-wheel"
    venv.EnvBuilder(with_pip=True, clear=True).create(env_dir)
    bin_dir = "Scripts" if sys.platform == "win32" else "bin"
    python = env_dir / bin_dir / ("python.exe" if sys.platform == "win32" else "python")
    # --no-deps: dev-залежності (pytest, ruff) у чисте середовище не потрапляють.
    subprocess.run(
        [str(python), "-m", "pip", "install", "--no-deps", str(wheels[-1])], check=True
    )
    output = ROOT / "reports" / "wheel-summary.txt"
    subprocess.run(
        [
            str(python),
            "-m",
            "kzp_lab07.cli",
            str(ROOT / "data" / "input.csv"),
            str(output),
        ],
        check=True,
        cwd=env_dir,
    )
    expected = (ROOT / "baseline" / "summary.txt").read_text(encoding="utf-8")
    if output.read_text(encoding="utf-8") != expected:
        print("Звіт зі встановленого wheel не збігається з еталоном", file=sys.stderr)
        return 1
    print(f"Wheel {wheels[-1].name} встановлено, звіт збігається з еталоном")
    return 0


if __name__ == "__main__":
    sys.exit(main())
