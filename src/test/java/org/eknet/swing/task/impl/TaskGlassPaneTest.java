package org.eknet.swing.task.impl;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;

import org.eknet.swing.task.Mode;
import org.eknet.swing.task.TaskManager;
import org.eknet.swing.task.ui.TaskBar;
import org.eknet.swing.task.ui.TaskGlassPane;

/**
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 30.09.11 19:11
 */
public class TaskGlassPaneTest {

    public static void main(String[] args) throws InterruptedException {
        final TaskManager tm = new TaskManagerImpl();

      JFrame frame = new JFrame("Test");
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.getContentPane().setLayout(new FlowLayout());
      JButton button = new JButton("Go");
      frame.getContentPane().add(button);
      frame.getContentPane().add(new TaskBar(tm));
      button.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          new Thread(new Runnable() {
            @Override
            public void run() {
              tm.create(new LongTask(Mode.BLOCKING)).execute();
              sleep(1000);
              tm.create(new LongTask(Mode.BLOCKING)).execute();
              sleep(1000);
              tm.create(new LongTask(Mode.BLOCKING)).execute();
            }
          }).start();
        }
      });
      frame.setGlassPane(new TaskGlassPane(tm));
      frame.setSize(1024, 768);
      frame.setVisible(true);

    }

  private static void sleep(long ms) {
    try {
      Thread.sleep(ms);
    } catch (InterruptedException e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    }
  }
}
