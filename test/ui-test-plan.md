# UI Test Plan

This file is the source of truth for Shan's console UI tests. Test cases run in document order. Each test case starts Shan in a fresh process, while the inputs within one test case share the same application state.

## Test configuration

- Required Java version: 25
- Compile command: `javac -d <temporary-class-directory> src/main/java/*.java`
- Run command: `java -cp <temporary-class-directory> Shan`
- Comparison: exact text after converting CRLF line endings to LF and excluding the terminal's echo of submitted input
- Failure policy: terminate the current process and stop the complete test run on the first mismatch

## Test cases

### UI-001: Manage different task types

**Aim:** Verify that Shan can add, mark, unmark, and list ToDo, Deadline, and Event tasks in one session.

**Inputs:**

1. `todo read book`
2. `mark 1`
3. `deadline return book /by Sunday`
4. `event project meeting /from Mon 2pm /to 4pm`
5. `unmark 1`
6. `list`
7. `bye`

**Expected outputs:**

1. Output caused by `todo read book`:

```text
____________________________________________________________
Shan: I Gotchu. I've added this:
  [T][ ] read book
Now you have 1 tasks.
____________________________________________________________
```

2. Output caused by `mark 1`:

```text
____________________________________________________________
Shan: Well done! I have marked this task as done!
  [T][X] read book
____________________________________________________________
```

3. Output caused by `deadline return book /by Sunday`:

```text
____________________________________________________________
Shan: I Gotchu. I've added this:
  [D][ ] return book (by: Sunday)
Now you have 2 tasks.
____________________________________________________________
```

4. Output caused by `event project meeting /from Mon 2pm /to 4pm`:

```text
____________________________________________________________
Shan: I Gotchu. I've added this:
  [E][ ] project meeting (from: Mon 2pm to: 4pm)
Now you have 3 tasks.
____________________________________________________________
```

5. Output caused by `unmark 1`:

```text
____________________________________________________________
Shan: What happened? I have unmarked this task as completed...
  [T][ ] read book
____________________________________________________________
```

6. Output caused by `list`:

```text
____________________________________________________________
Shan: Here are the tasks in your list:
1.[T][ ] read book
2.[D][ ] return book (by: Sunday)
3.[E][ ] project meeting (from: Mon 2pm to: 4pm)
____________________________________________________________
```

7. Output caused by `bye`:

```text
____________________________________________________________
Shan: Bye! See you soon.
____________________________________________________________
```

### UI-002: Reject malformed task commands

**Aim:** Verify that missing descriptions and task delimiters produce helpful messages without adding tasks or terminating Shan.

**Inputs:**

1. `todo`
2. `deadline return book`
3. `deadline /by Sunday`
4. `event project meeting`
5. `event project meeting /from Mon 2pm`
6. `event /from Mon 2pm /to 4pm`
7. `list`
8. `bye`

**Expected outputs:**

1. Output caused by `todo`:

```text
____________________________________________________________
Shan: The task description cannot be empty my guy.
____________________________________________________________
```

2. Output caused by `deadline return book`:

```text
____________________________________________________________
Shan: Please specify a deadline using /by.
____________________________________________________________
```

3. Output caused by `deadline /by Sunday`:

```text
____________________________________________________________
Shan: The deadline description and date cannot be empty bruh.
____________________________________________________________
```

4. Output caused by `event project meeting`:

```text
____________________________________________________________
Shan: Specify the event start using /from.
____________________________________________________________
```

5. Output caused by `event project meeting /from Mon 2pm`:

```text
____________________________________________________________
Shan: Specify the event end using /to.
____________________________________________________________
```

6. Output caused by `event /from Mon 2pm /to 4pm`:

```text
____________________________________________________________
Shan: The event description, start, and end cannot be empty, lock in bro.
____________________________________________________________
```

7. Output caused by `list`:

```text
____________________________________________________________
Shan: Here are the tasks in your list:
____________________________________________________________
```

8. Output caused by `bye`:

```text
____________________________________________________________
Shan: Bye! See you soon.
____________________________________________________________
```

### UI-003: Reject empty command arguments

**Aim:** Verify that every command requiring an argument reports an error through Shan's exception handling and leaves the application running.

**Inputs:**

1. `todo`
2. `deadline`
3. `event`
4. `mark`
5. `unmark`
6. `mark one`
7. `bye`

**Expected outputs:**

1. Output caused by `todo`:

```text
____________________________________________________________
Shan: The task description cannot be empty my guy.
____________________________________________________________
```

2. Output caused by `deadline`:

```text
____________________________________________________________
Shan: The deadline description cannot be empty, else its not a deadline
____________________________________________________________
```

3. Output caused by `event`:

```text
____________________________________________________________
Shan: The event description cannot be empty...
____________________________________________________________
```

4. Output caused by `mark`:

```text
____________________________________________________________
Shan: Specify a task number.
____________________________________________________________
```

5. Output caused by `unmark`:

```text
____________________________________________________________
Shan: Specify a task number.
____________________________________________________________
```

6. Output caused by `mark one`:

```text
____________________________________________________________
Shan: The task number must be an int.
____________________________________________________________
```

7. Output caused by `bye`:

```text
____________________________________________________________
Shan: Bye! See you soon.
____________________________________________________________
```
