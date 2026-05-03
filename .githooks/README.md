# Local git hooks for AugerLink

Two hooks that block private-workspace context from landing in commits or commit messages. They run client-side; there is no server-side enforcement. Each contributor enables them once per clone.

## What they catch

The pattern file [`private-context-patterns`](private-context-patterns) is a list of POSIX extended regexes (one per line) that should never appear in this public repository. Categories covered:

- **Infrastructure paths** — workspace directories outside this repo's tree
- **Identifier references** — task / experiment / mission / cycle / idea numbering from the agent's private tracker
- **Process / cultural language** — framework-specific reified concepts (`AUTO_MODE`, `MANUAL_MODE`, `JOTEWR`, `CCP`, `DEV_DRV`, `DELEG_DRV`)
- **Hash-form breadcrumbs** — `s_…/c_…/p_…/t_…` session/cycle/prompt/timestamp tuples

The pattern file is the single source of truth. To refine, edit it directly — both hooks read from it at runtime.

## How they work

- [`pre-commit`](pre-commit) — scans **added lines** in staged changes (`+` lines from `git diff --cached`, excluding `+++` diff headers). The `.githooks/` directory itself is excluded from scanning so the patterns file + hook scripts don't self-trip.
- [`commit-msg`](commit-msg) — scans the commit message file (`$1`), skipping comment lines.

Either hook blocks the commit with a clear "what to do" message when a pattern matches.

## Enabling

After cloning the repo, run once:

```
git config core.hooksPath .githooks
```

This is local-only `.git/config` state; you don't need to redo it after rebases, branch switches, or pulls, but each contributor must enable it once per clone. Git deliberately does not let a repository auto-enable hooks for security reasons.

To verify hooks are active:

```
git config core.hooksPath
# expect: .githooks
```

## Override

Rare cases need an override — for example, a commit body that intentionally quotes a path being scrubbed (the recursive case where describing the leak requires showing it). Use the env var:

```
ALLOW_PRIVATE_CONTEXT=1 git commit ...
```

Default to fixing the leak rather than overriding. Overriding leaves a permanent leak in `git log` that's expensive to clean up later.

## Why these patterns

AugerLink was built inside an agent's private workspace alongside framework-specific tracking infrastructure. Before any artifact crosses to a public repo, four layers of context need translation: workspace paths, identifier references, process language, and breadcrumbs. These hooks catch the obvious cases automatically; they do not replace human review of ambiguous cases.

If you find a false positive (a legitimate term flagged as a leak), open an issue or send a PR refining the pattern.
