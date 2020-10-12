package SIPVS.model;


import javafx.beans.property.SimpleStringProperty;

public class Borrow {
    private SimpleStringProperty name;
    private SimpleStringProperty borrowDate;
    private SimpleStringProperty returnedDate;

    public Borrow(String name, String borrowDate, String returnedDate) {
        this.name = new SimpleStringProperty(name);
        this.borrowDate = new SimpleStringProperty(borrowDate);
        this.returnedDate = new SimpleStringProperty(returnedDate);
    }


    public String getName() {
        return name.get();
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public String getBorrowDate() {
        return borrowDate.get();
    }

    public SimpleStringProperty borrowDateProperty() {
        return borrowDate;
    }

    public void setBorrowDate(String borrowDate) {
        this.borrowDate.set(borrowDate);
    }

    public String getReturnedDate() {
        return returnedDate.get();
    }

    public SimpleStringProperty returnedDateProperty() {
        return returnedDate;
    }

    public void setReturnedDate(String returnedDate) {
        this.returnedDate.set(returnedDate);
    }
}
