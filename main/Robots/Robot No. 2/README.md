# Robot No. 2

### CvPanel.java
- A simple class that extends JPanel in order to display a BufferedImage.
### Main.java
- We are not supposed to change this class. It is for the initialization of the robot.
### MyVector.java
- A minimalistic vector class that contains double values for x, y, and angle.
### ProcessImage.java
- A utility class containing only static functions. This uses opencv to process an image to find contours and use those contours to determine where the robot should move.
### Robot.java
- The main file. This file contains the code necessary to interact with the RoboRIO.
