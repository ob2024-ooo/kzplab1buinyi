"""Пакет обробки CSV реєстру студентів (лабораторна робота № 6, варіант 6)."""

from kzp_lab06.analysis import make_summary
from kzp_lab06.errors import DataError
from kzp_lab06.loader import load_records

__all__ = ["DataError", "load_records", "make_summary"]
__version__ = "0.1.0"
