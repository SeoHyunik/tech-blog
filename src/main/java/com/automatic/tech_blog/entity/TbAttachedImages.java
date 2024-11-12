package com.automatic.tech_blog.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.sql.Timestamp;
import lombok.Data;

@Entity
@Table(name = "tb_attached_images")
@Data
public class TbAttachedImages {
  @Id
    private int id;
    private int fileId;
    private String imagePath;
    private String wpUrl;
    private Timestamp uploadedAt;
}
