package vn.iotstar.ktqt03.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Nationalized;

@Entity
@Table(name="Category", schema="dbo")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="CategoryId")
    private Integer categoryId;

    @Nationalized
    @Column(name="Categoryname", length=100)
    private String categoryName;

    @Nationalized
    @Column(name="Categorycode", length=100)
    private String categoryCode;

    @Nationalized
    @Column(name="Images", length=500)
    private String images;

    @Column(name="Status")
    private Boolean status;

    public Category() {}

    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getCategoryCode() { return categoryCode; }
    public void setCategoryCode(String categoryCode) { this.categoryCode = categoryCode; }

    public String getImages() { return images; }
    public void setImages(String images) { this.images = images; }

    public Boolean getStatus() { return status; }
    public void setStatus(Boolean status) { this.status = status; }
}
