package org.eknet.swing.task.impl;

import java.awt.BorderLayout;

import javax.swing.JFrame;

import org.eknet.swing.task.Mode;
import org.eknet.swing.task.TaskControl;
import org.eknet.swing.task.TaskManager;
import org.eknet.swing.task.ui.TaskGlassPane;

/**
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 30.09.11 19:11
 */
public class TaskGlassPaneTest {

    public static void main(String[] args) throws InterruptedException {
        TaskManager tm = new TaskManagerImpl();

        JFrame frame = new JFrame("Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setGlassPane(new TaskGlassPane(tm));
        frame.setSize(1024, 768);
        frame.setVisible(true);
        tm.create(new LongTask(Mode.BLOCKING)).execute();
        Thread.sleep(2000);
        tm.create(new LongTask(Mode.BLOCKING)).execute();
    }
}
