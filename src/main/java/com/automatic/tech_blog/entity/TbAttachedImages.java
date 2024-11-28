package com.automatic.tech_blog.entity;

import jakarta.persistence.*;
import java.util.Date;
import lombok.Data;
import java.sql.Timestamp;

@Entity
@Table(name = "tb_attached_images")
@Data
public class TbAttachedImages {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  @Column(name = "parent_file_id", nullable = true)
  private String parentFileId;

  @Column(name = "image_id", nullable = false)
  private String imageId;

  @Column(name = "image_name", nullable = false)
  private String imageName;

  @Column(name = "created_at", nullable = true)
  private Date createdAt;

  @Column(name = "uploaded_at", nullable = true)
  private Date uploadedAt;
}
