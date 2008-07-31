/*
 * JBoss, the OpenSource J2EE webOS
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.test.ws.benchmark.jaxrpc;

/**
 * A LineItem.
 * 
 * @author <a href="anders.hedstrom@home.se">Anders Hedstrom</a>
 */
public class LineItem {
    protected int orderId;
    protected int itemId;
    protected int productId;
    protected String productDescription;
    protected int orderQuantity;
    protected float unitPrice;
    
    public LineItem() {
    }
    
    public LineItem(int orderId, int itemId, int productId, String productDescription, int orderQuantity, float unitPrice) {
        this.orderId = orderId;
        this.itemId = itemId;
        this.productId = productId;
        this.productDescription = productDescription;
        this.orderQuantity = orderQuantity;
        this.unitPrice = unitPrice;
    }
    
    public int getOrderId() {
        return orderId;
    }
    
    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }
    
    public int getItemId() {
        return itemId;
    }
    
    public void setItemId(int itemId) {
        this.itemId = itemId;
    }
    
    public int getProductId() {
        return productId;
    }
    
    public void setProductId(int productId) {
        this.productId = productId;
    }
    
    public String getProductDescription() {
        return productDescription;
    }
    
    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }
    
    public int getOrderQuantity() {
        return orderQuantity;
    }
    
    public void setOrderQuantity(int orderQuantity) {
        this.orderQuantity = orderQuantity;
    }
    
    public float getUnitPrice() {
        return unitPrice;
    }
    
    public void setUnitPrice(float unitPrice) {
        this.unitPrice = unitPrice;
    }
}
