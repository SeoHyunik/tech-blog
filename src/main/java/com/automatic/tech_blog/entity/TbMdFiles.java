package com.automatic.tech_blog.entity;

import com.google.api.client.util.DateTime;
import jakarta.persistence.*;
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
  private DateTime createdAt;

  @Column(name = "modified_at", nullable = true)
  private DateTime modifiedAt;

  @Column(name = "deleted_at", nullable = true)
  private DateTime deletedAt;
}

