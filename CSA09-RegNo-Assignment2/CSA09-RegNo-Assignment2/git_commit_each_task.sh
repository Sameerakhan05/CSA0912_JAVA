#!/bin/bash
# Run once inside the repository folder after: git init && git remote add origin <your repo URL>
# Creates one commit per task so the commit history shows progressive work.
set -e
git add README.md                    && git commit -m "Add README with name, register number and run steps"
git add Task01                       && git commit -m "Q1: String vs StringBuilder vs StringBuffer benchmark and results"
git add Task02                       && git commit -m "Q2: ArrayList vs LinkedList vs ArrayDeque experiments"
git add Task03/before                && git commit -m "Q3: Attendance run BEFORE hashCode (prints 3)"
git add Task03                       && git commit -m "Q3: Attendance AFTER hashCode (prints 2) and contract tests"
git add Task04                       && git commit -m "Q4: HashMap/TreeMap/LinkedHashMap pharmacy benchmark"
git add Task05                       && git commit -m "Q5: Generic leaderboard, comparator chain and contract checks"
git add Task06                       && git commit -m "Q6: Priority queue triage, starvation and aging"
git add Task07                       && git commit -m "Q7: Diagnose, redesign (cached sorted index + merge) and validate"
git add .                            && git commit -m "Add report and remaining files" || true
git branch -M main && git push -u origin main
