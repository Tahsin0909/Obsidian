
if [ $# -eq 0 ]; then
  echo "Please provide a commit message."
  echo "Usage: ./commit.sh \"Your commit message\""
  exit 1
fi

COMMIT_MSG="$*"

git add .
git commit -m "$COMMIT_MSG"
git push