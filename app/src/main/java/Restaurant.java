public class Restaurant {

    private String name;
    private String type;
    private String address;
    private boolean isFavorite;

       public Restaurant(String name, String type, String address, boolean isFavorite) {
        this.name = name;
        this.type = type;
        this.address = address;
        this.isFavorite = isFavorite;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getAddress() {
        return address;
    }

    public boolean isFavorite() {
        return isFavorite;
    }
}

