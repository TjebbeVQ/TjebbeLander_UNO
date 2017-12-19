package server;

public class Card {
    private Integer id;
    private String color;
    private Integer value;
    private Integer cost;
    private String action;

    public Card(Integer id, String color, Integer value, Integer cost, String action) {
        this.id = id;
        this.color = color;
        this.value = value;
        this.cost = cost;
        this.action = action;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }


    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public Integer getCost() {
        return cost;
    }

    public void setCost(Integer cost) {
        this.cost = cost;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public boolean kanOp(Card bovensteKaart) {
        //controleer of deze kaart kan gelegd worden op de meegegeven kaart
        if(this.color == bovensteKaart.getColor()){
            return true;
        }else if(this.value == bovensteKaart.getValue() && this.value!=null){
            return true;
        }else if(this.action.split("_")[1].equals(bovensteKaart.action.split("_")[1])){
            return true;
        }else if(this.action == "+4" || this.action == "picker"){
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        /*return "Card{" +
                "id=" + id +
                ", color='" + color + '\'' +
                ", value=" + value +
                ", cost=" + cost +
                ", action='" + action + '\'' +
                '}';
                */
        return action;
    }

}
