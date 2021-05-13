import java.util.ArrayList;

public class Person {

    private String name;
    private ArrayList<Person> preferences = new ArrayList<>();
    private ArrayList<Person> avoid = new ArrayList<>();

    public Person (String name){
        this.name = name;
    }

    public void addPreference(String name){
        Person p = Main.getPersonByName(name);
        addPreference(p);
    }

    public void addPreference(Person p){
        if (this == p){
            System.out.println("Warning: " + this + " cannot prefer themself");
            return;
        }
        if(!prefers(p))
            preferences.add(p);
        else
            System.out.println("Warning: " + this + " already prefers " + p.getName());
    }

    public String getName() {
        return name;
    }

    public boolean prefers(Person p){
        return preferences.contains(p);
    }

    public boolean prefers(String name){
        Person p = Main.getPersonByName(name);
        return prefers(p);
    }

    @Override
    public String toString() {

        String prefString = "", avoidString = "";
        if (preferences.size() > 0){
            for(Person p : preferences){
                prefString += p.getName() + ", ";
            }
            prefString = " prefs: " + prefString.substring(0, prefString.length()-2) + " ";
        }

        if(avoid.size() > 0){
            for(Person p : avoid){
                avoidString += p.getName() + ", ";
            }
            avoidString = " avoid: " + avoidString.substring(0, avoidString.length()-2) + " ";
        }

        return "Person{'" + name + "'" + prefString + avoidString + '}';
    }

    public void avoid(String thisName) {
        Person p = Main.getPersonByName(thisName);
        avoid.add(p);
    }

    public boolean avoids(Person person) {
        return avoid.contains(person);
    }
}
