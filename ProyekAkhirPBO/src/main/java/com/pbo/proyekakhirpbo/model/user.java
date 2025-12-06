/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.pbo.proyekakhirpbo.model;

/**
 *
 * @author joan clarissa hal
 */
public class user {
    private int id_user;
    private String nama;
    private String email;
    private String no_telp;
    private String password;
    private String role;
    
    public user(){}
    
    public user(int id_user, String nama, String email, String no_telp, String password, String role){
        this.id_user = id_user;
        this.nama = nama;
        this.email = email;
        this.no_telp = no_telp;
        this.password = password;
        this.role = role;
    }
    
    public int getId(){
        return id_user;
    }
    
    public void setId(int id_user){
        this.id_user = id_user;
    }
    
    public String getNama(){
        return nama;
    }
    
    public void setNama(String nama){
        this.nama = nama;
    }
    
    public String getEmail(){
        return email;
    }
    
    public void setEmail(String email){
        this.email = email;
    }
    
    public String getNoTelp(){
        return no_telp;
    }
    
    public void setNoTelp(String no_telp){
        this.no_telp = no_telp;
    }
    
    public String getPassword(){
        return password;
    }
    
    public void setPassword(String password){
        this.password = password;
    }
    
    public String getRole(){
        return role;
    }
    
    public void setRole(String role){
        this.role = role;
    }
    
          
}
