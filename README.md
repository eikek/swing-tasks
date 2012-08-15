swing-tasks
===========

This is a small library that aims to help with executing tasks from swing applications. It consist of a set of
classes that build around the `SwingWorker` class.


Dependencies
------------

The only dependency is to the slf4j logging api.


Compile
-------

Use `maven` to compile the sources:

    mvn install


Usage
-----

The class `TaskManager` is the main entry point. Usually, you would use this as an application wide
singleton. The `TaskManager` is used to submit tasks that implement either `Task`, `Callable` or
`Runnable`. It can query currently active tasks and can be used to register listeners that receive
events from running tasks.

    TaskManager taskManager = new TaskManagerImpl();

For swing related tasks, it is recommended to implement the `Task` interface, as it mimics the
`SwingWorker` interface and thus provides some features regarding swing. The `TaskManager` creates
a `TaskControl` object for each `Task` that provides the usual control methods and additionally
defines a method to get the `TaskContext` of the current task. The `TaskContext` can be used to
get some information about the task and to add listeners that receive events for this task only.

If a `Task` is submitted a `TaskControl` object is received in response. But task has not been
started yet and is still in state _PENDING_. In order to start the task, call `execute()` on the
`TaskControl` object. The idea is to be able to register listeners for exactly this task prior to
starting it. Example:

    Task<Long, Long> task = new LongTask();
    TaskControl<Long> control = taskManager.create(task);
    control.getContext().addListener(new TaskListener() {
      @Override
      public void stateChanged(ChangeEvent<State> event) {
        log.info(">>> State: " + event.getOldValue() + " => " + event.getNewValue());
        log.info("Started: " + event.getSource().getStartedTimestamp());
      }

      @Override
      public void progressChanged(ChangeEvent<Integer> event) {
        log.info(">>> Progress: " + event.getOldValue() + " => " + event.getNewValue());
      }

      @Override
      public void phaseChanged(ChangeEvent<String> event) {
        log.info(">>> Phase: " + event.getOldValue() + " => " + event.getNewValue());
      }
    });
    Long value = control.waitFor();
    log.info("Waited for task: " + value);

The package `org.eknet.swing.task.ui` provides some simple swing ui classes for displaying running task and
a default glass pane. You can use the glass pane with a `JFrame`. It will popup if any task of mode _BLOCKING_
is executed and shows a list of tasks currently running -- as well as a button to cancel them.

    JFrame frame = new JFrame("my app");
    frame.setGlassPane(new TaskGlassPane(taskManager));