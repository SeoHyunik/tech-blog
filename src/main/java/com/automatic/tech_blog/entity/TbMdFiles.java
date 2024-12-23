package com.automatic.tech_blog.entity;

import jakarta.persistence.*;
import java.util.Date;
import lombok.Data;

@Entity
@Table(name = "tb_md_files")
@Data
public class TbMdFiles {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  @Column(unique = true, nullable = false)
  private String fileId;

  private String fileName;
  private String filePath;

  @Column(name = "created_at")
  private Date createdAt;

  @Column(name = "modified_at", nullable = true)
  private Date modifiedAt;

  @Column(name = "deleted_at", nullable = true)
  private Date deletedAt;
}
