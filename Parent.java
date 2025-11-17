package com.example.b07project.model;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
public class Parent extends User{
    private final List<Child> children = new ArrayList<>();

    public Parent(String displayName) {
        super(displayName, UserRole.PARENT);
    }

    public void linkChild(Child child) {
        if (!children.contains(child)) {
            children.add(child);
        }
    }

    public void unlinkChild(Child child) {
        children.remove(child);
    }

    public List<Child> getChildren() {
        return Collections.unmodifiableList(children);
    }
}
