package project;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class Movie {
    String name;
    int seatsAvailable;
    double costPerTicket;
    String showTime;

    Movie(String name, int seatsAvailable, double costPerTicket, String showTime) {
        this.name = name;
        this.seatsAvailable = seatsAvailable;
        this.costPerTicket = costPerTicket;
        this.showTime = showTime;
    }
}

class Customer {
    String name;
    String phoneNumber;
    int ticketsRequired;
	public String totalCost;

    Customer(String name, String phoneNumber, int ticketsRequired) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.ticketsRequired = ticketsRequired;
    }
}

class Booking {
    Customer customer;
    Movie movie;
    int totalCost;

    Booking(Customer customer, Movie movie, int totalCost) {
        this.customer = customer;
        this.movie = movie;
        this.totalCost = totalCost;
    }
}

class BookingSystemGUI extends JFrame implements ActionListener {
    List<Movie> movies = new ArrayList<>();
    List<Booking> bookings = new ArrayList<>();
    JTextArea displayArea;
    JTextField nameField;
    JTextField phoneNumberField;
    JTextField ticketsRequiredField;
    JComboBox<String> movieNameComboBox;
    JTextField cancelPhoneNumberField;

    BookingSystemGUI() {
        setTitle("Movie Booking System");
        setSize(720,720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(7, 2));

        nameField = new JTextField(10);
        panel.add(new JLabel("Enter your name:"));
        panel.add(nameField);

        phoneNumberField = new JTextField(10);
        panel.add(new JLabel("Enter your phone number:"));
        panel.add(phoneNumberField);

        ticketsRequiredField = new JTextField(10);
        panel.add(new JLabel("Enter the number of tickets required:"));
        panel.add(ticketsRequiredField);

        movieNameComboBox = new JComboBox<>();
        panel.add(new JLabel("Select the movie you want to watch:"));
        panel.add(movieNameComboBox);

        JButton bookButton = new JButton("Book Ticket");
        bookButton.addActionListener(this);
        panel.add(bookButton);

        cancelPhoneNumberField = new JTextField(10);
        panel.add(new JLabel("Enter your phone number to cancel booking:"));
        panel.add(cancelPhoneNumberField);

        JButton cancelButton = new JButton("Cancel Booking");
        cancelButton.addActionListener(this);
        panel.add(cancelButton);

        JButton displayButton = new JButton("Display available movies");
        displayButton.addActionListener(this);
        panel.add(displayButton);

        add(panel, BorderLayout.NORTH);

        displayArea = new JTextArea();
        JScrollPane scrollPane = new JScrollPane(displayArea);
        add(scrollPane, BorderLayout.CENTER);

        setVisible(true);
        
        resetFields();
        
    }

    void addMovie(Movie movie) {
        movies.add(movie);
        //movieNameComboBox.addItem(movie.name);
    }
    
    void populateMovieComboBox() {
        movieNameComboBox.removeAllItems(); // Clear existing items
        for (Movie movie : movies) {
            movieNameComboBox.addItem(movie.name);
        }
    }

    void displayMovies() {
        StringBuilder sb = new StringBuilder();
        for (Movie movie : movies) {
            sb.append("-------------------------\n");
            sb.append("Movie: ").append(movie.name).append("\n");
            sb.append("Seats Available: ").append(movie.seatsAvailable).append("\n");
            sb.append("Cost per Ticket: ").append(movie.costPerTicket).append("\n");
            sb.append("Show Time: ").append(movie.showTime).append("\n");
            sb.append("-------------------------\n");
        }
        displayArea.setText(sb.toString());
    }

    
    void resetFields() {
        nameField.setText("");
        phoneNumberField.setText("");
        ticketsRequiredField.setText("");
        cancelPhoneNumberField.setText("");
        movieNameComboBox.setSelectedIndex(-1); // Reset movie selection
    }

     void bookTicket(String name, String phoneNumber, String ticketsRequiredStr, String movieName) {
        int ticketsRequired;
        try {
            ticketsRequired = Integer.parseInt(ticketsRequiredStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number for tickets required.");
            return;
        }

        if (ticketsRequired <= 0) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number of tickets.");
            return;
        }

        String selectedMovieName = (String) movieNameComboBox.getSelectedItem();
        Movie chosenMovie = null;
        for (Movie movie : movies) {
            if (movie.name.equalsIgnoreCase(selectedMovieName)) {
                chosenMovie = movie;
                break;
            }
        }

        if (chosenMovie != null) {
            int totalCost = (int)chosenMovie.costPerTicket * ticketsRequired;
            Customer customer = new Customer(name, phoneNumber, ticketsRequired);
            if (chosenMovie.seatsAvailable >= ticketsRequired) {
                chosenMovie.seatsAvailable -= ticketsRequired;
                bookings.add(new Booking(customer, chosenMovie, totalCost));
                saveBooking(customer, chosenMovie.name, totalCost);
                JOptionPane.showMessageDialog(this, "Ticket booked successfully for " + name + "\nMovie: " + chosenMovie.name + "\nTotal Cost: " + totalCost);
                resetFields();
            } else {
                JOptionPane.showMessageDialog(this, "Sorry, not enough seats available for " + chosenMovie.name);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Sorry, we couldn't find a movie with that name.");
        }
        
    }


    void cancelBooking(String phoneNumber) {
        Iterator<Booking> iterator = bookings.iterator();
        boolean bookingCancelled = false;
        while (iterator.hasNext()) {
            Booking booking = iterator.next();
            if (booking.customer.phoneNumber.equals(phoneNumber)) {
                booking.movie.seatsAvailable += booking.customer.ticketsRequired;
                iterator.remove();
                removeBooking(booking.customer);
                bookingCancelled = true;
            }
        }
        if (bookingCancelled) {
            JOptionPane.showMessageDialog(this, "All bookings for phone number " + phoneNumber + " have been cancelled.");
            resetFields();
        } else {
            JOptionPane.showMessageDialog(this, "No bookings found for phone number " + phoneNumber);
        }
    }

    void saveBooking(Customer customer, String movieName, int totalCost) {
        try (FileWriter writer = new FileWriter("bookings.csv", true)) {
            writer.append(customer.name)
                  .append(',')
                  .append(customer.phoneNumber)
                  .append(',')
                  .append(Integer.toString(customer.ticketsRequired))
                  .append(',')
                  .append(movieName)
                  .append(',')
                  .append(Integer.toString(totalCost))
                  .append('\n');
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void removeBooking(Customer customer) {
        try {
            File inputFile = new File("bookings.csv");
            File tempFile = new File("temp.csv");

            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

            String lineToRemove = customer.name + "," + customer.phoneNumber + "," + customer.ticketsRequired; // Adjusted lineToRemove

            String currentLine;

            while ((currentLine = reader.readLine()) != null) {
                // Trim newline when comparing with lineToRemove
                String trimmedLine = currentLine.trim();
                if (!trimmedLine.startsWith(lineToRemove)) { // Adjusted condition to check start of the line
                    writer.write(currentLine + System.getProperty("line.separator"));
                }
            }
            writer.close();
            reader.close();

            // Delete the original file
            if (!inputFile.delete()) {
                System.out.println("Could not delete file");
                return;
            }

            // Rename the new file to the filename the original file had.
            if (!tempFile.renameTo(inputFile)) {
                System.out.println("Could not rename file");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("Display available movies")) {
            displayMovies();
            populateMovieComboBox();
        } else if (e.getActionCommand().equals("Book Ticket")) {
            String name = nameField.getText();
            String phoneNumber = phoneNumberField.getText();
            String ticketsRequired = ticketsRequiredField.getText();
            String movieName = (String) movieNameComboBox.getSelectedItem();
            if (!name.isEmpty() && !phoneNumber.isEmpty() && !ticketsRequired.isEmpty() && movieName != null) {
                bookTicket(name, phoneNumber, ticketsRequired, movieName);
            } else {
                JOptionPane.showMessageDialog(this, "Please fill in all the fields.");
            }
        } else if (e.getActionCommand().equals("Cancel Booking")) {
            String phoneNumber = cancelPhoneNumberField.getText();
            if (!phoneNumber.isEmpty()) {
                cancelBooking(phoneNumber);
            } else {
                JOptionPane.showMessageDialog(this, "Please enter a phone number.");
            }
        }
    }
}

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                BookingSystemGUI bookingSystemGUI = new BookingSystemGUI();
                // Adding sample movies
                bookingSystemGUI.addMovie(new Movie("Oppenheimer", 100, 150.0, "01:00 PM"));
                bookingSystemGUI.addMovie(new Movie("Barbie", 100, 200.0, "04:00 PM"));
                bookingSystemGUI.addMovie(new Movie("Avatar", 100, 250.0, "07:00 PM"));
                bookingSystemGUI.addMovie(new Movie("RRR", 100, 150.0, "10:00 AM"));
                bookingSystemGUI.addMovie(new Movie("Pathan", 100, 150.0, "10:00 AM"));
            }
        });
    }
}


