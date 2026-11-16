"""Пакет об'єктної обробки CSV реєстру студентів (лабораторна №7, варіант 6)."""

from kzp_lab07.analysis import make_summary
from kzp_lab07.errors import DataError
from kzp_lab07.loader import load_learners
from kzp_lab07.models import Bachelor, GroupRating, Learner, LearnerSummary, Master

__all__ = [
    "Bachelor",
    "DataError",
    "GroupRating",
    "Learner",
    "LearnerSummary",
    "Master",
    "load_learners",
    "make_summary",
]
__version__ = "0.2.0"
