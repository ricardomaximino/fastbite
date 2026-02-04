package es.brasatech.fastbite.mongodb.order;

import es.brasatech.fastbite.order.mongodb.ProductCustomizerDocument;

import java.math.BigDecimal;
import java.util.List;

public class CartItemDocument {

    private String id;
    private String itemId;
    private String name;
    private String description;
    private String image;
    private int quantity;
    private List<ProductCustomizerDocument> customizations;
    private BigDecimal price;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public List<ProductCustomizerDocument> getCustomizations() {
        return customizations;
    }

    public void setCustomizations(List<ProductCustomizerDocument> customizations) {
        this.customizations = customizations;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
