/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.pbo.proyekakhirpbo.ui;

/**
 *
 * @author acer
 */

import java.io.File;
import java.io.FileInputStream;
import javax.swing.JOptionPane;
import com.pbo.proyekakhirpbo.db.Konektor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class FrameDashboard extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(FrameDashboard.class.getName());
    private String userEmail;
    /**
     * Creates new form FrameDashboard
     */
    
    public FrameDashboard(String email){
        this.userEmail = email;
        initComponents();
        setHaloNama();
        loadProduk("");
    }
    
    
    public FrameDashboard() {
        initComponents();
    }
    
    private void showProductDetail(int idProduk, String nama, double harga, String deskripsi, byte[] imgBytes) {
        javax.swing.JPanel panel = new javax.swing.JPanel();
        panel.setLayout(new java.awt.BorderLayout(10, 10));
        panel.setPreferredSize(new java.awt.Dimension(300, 400));

        
        javax.swing.JLabel lblImage = new javax.swing.JLabel();
        lblImage.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        if (imgBytes != null) {
            javax.swing.ImageIcon icon = new javax.swing.ImageIcon(imgBytes);
            java.awt.Image img = icon.getImage().getScaledInstance(200, 200, java.awt.Image.SCALE_SMOOTH);
            lblImage.setIcon(new javax.swing.ImageIcon(img));
        } else {
            lblImage.setText("No Image");
        }
        panel.add(lblImage, java.awt.BorderLayout.NORTH);
        javax.swing.JPanel infoPanel = new javax.swing.JPanel();
        infoPanel.setLayout(new javax.swing.BoxLayout(infoPanel, javax.swing.BoxLayout.Y_AXIS));
        
        javax.swing.JLabel lblNama = new javax.swing.JLabel(nama);
        lblNama.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 18));
        lblNama.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        
        javax.swing.JLabel lblHarga = new javax.swing.JLabel("Rp " + (long)harga);
        lblHarga.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        lblHarga.setForeground(java.awt.Color.GREEN);
        lblHarga.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        
        javax.swing.JTextArea txtDesc = new javax.swing.JTextArea(deskripsi);
        txtDesc.setWrapStyleWord(true);
        txtDesc.setLineWrap(true);
        txtDesc.setEditable(false);
        txtDesc.setBackground(panel.getBackground());
        txtDesc.setMargin(new java.awt.Insets(10, 10, 10, 10));
        
        infoPanel.add(lblNama);
        infoPanel.add(lblHarga);
        infoPanel.add(javax.swing.Box.createRigidArea(new java.awt.Dimension(0, 10))); 
        infoPanel.add(new javax.swing.JScrollPane(txtDesc)); 

        panel.add(infoPanel, java.awt.BorderLayout.CENTER);

        Object[] options = {"Add to Cart", "Cancel"};
        int result = javax.swing.JOptionPane.showOptionDialog(
            this, panel, "Detail Produk",
            javax.swing.JOptionPane.YES_NO_OPTION,
            javax.swing.JOptionPane.PLAIN_MESSAGE,
            null, options, options[0]
        );

        if (result == javax.swing.JOptionPane.YES_OPTION) {
            addToCart(idProduk);
        }
    }

    public void setHaloNama(){
        try{
            java.sql.Connection conn = com.pbo.proyekakhirpbo.db.Konektor.getConnection();
            String sql = "SELECT nama FROM user WHERE email = ?";
            java.sql.PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, userEmail);
            java.sql.ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                String namaUser = rs.getString("nama");
                haloLabel.setText("Hallo " + namaUser + " !");
            }
        }
        catch (Exception e) {
            haloLabel.setText("Hallo " + userEmail + " !");
        }
    }
    
    private void loadProduk(String keyword) {
        jPanel2.removeAll();
        jPanel2.setLayout(new java.awt.GridLayout(0, 5, 10, 10)); 

        try {
            java.sql.Connection conn = com.pbo.proyekakhirpbo.db.Konektor.getConnection();
            String sql;
            java.sql.PreparedStatement pst;

            if (keyword == null || keyword.isEmpty()) {
                sql = "SELECT * FROM produk"; 
                pst = conn.prepareStatement(sql);
            } else {
                sql = "SELECT * FROM produk WHERE nama_barang LIKE ?";
                pst = conn.prepareStatement(sql);
                pst.setString(1, "%" + keyword + "%"); 
            }

            java.sql.ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                int idProduk = rs.getInt("id_produk");
                String nama = rs.getString("nama_barang");
                double harga = rs.getDouble("harga_barang");
                String deskripsi = rs.getString("deskripsi");
                byte[] imgBytes = rs.getBytes("image_barang");

                javax.swing.JButton btn = new javax.swing.JButton();
                btn.setText("<html><center><b>" + nama + "</b><br>Rp " + (long)harga + "</center></html>");
                if (imgBytes != null) {
                    javax.swing.ImageIcon icon = new javax.swing.ImageIcon(imgBytes);
                    java.awt.Image img = icon.getImage().getScaledInstance(120, 120, java.awt.Image.SCALE_SMOOTH);
                    btn.setIcon(new javax.swing.ImageIcon(img));
                }
                btn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
                btn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
                btn.setPreferredSize(new java.awt.Dimension(150, 170));

                btn.addActionListener(e -> {
                    showProductDetail(idProduk, nama, harga, deskripsi, imgBytes);
                });

                jPanel2.add(btn);
            }

            jPanel2.revalidate();
            jPanel2.repaint();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void addToCart(int idProduk) {
        System.out.println("DEBUG CHECK: Searching for user with email: [" + userEmail + "]");
        try {
            Connection conn = com.pbo.proyekakhirpbo.db.Konektor.getConnection();

            String userSql = "SELECT id_user FROM user WHERE email = ?";
            PreparedStatement userPst = conn.prepareStatement(userSql);
            userPst.setString(1, userEmail);
            ResultSet userRs = userPst.executeQuery();

            int idUser = -1;
            if (userRs.next()) {
                idUser = userRs.getInt("id_user");
            } else {
                javax.swing.JOptionPane.showMessageDialog(this, "Error: User tidak ditemukan!");
                return;
            }

            String checkSql = "SELECT * FROM keranjang WHERE id_user = ? AND id_produk = ?";
            PreparedStatement checkPst = conn.prepareStatement(checkSql);
            checkPst.setInt(1, idUser);
            checkPst.setInt(2, idProduk);
            ResultSet checkRs = checkPst.executeQuery();

            if (checkRs.next()) {
                javax.swing.JOptionPane.showMessageDialog(this, "Barang sudah ada di keranjang!");
            } else {
                String insertSql = "INSERT INTO keranjang (id_user, id_produk, kuantitas) VALUES (?, ?, 1)";
                PreparedStatement insertPst = conn.prepareStatement(insertSql);
                insertPst.setInt(1, idUser);
                insertPst.setInt(2, idProduk);
                insertPst.executeUpdate();
                
                javax.swing.JOptionPane.showMessageDialog(this, "Berhasil masuk keranjang!");
            }
        } catch (Exception e) {
            System.out.println("Error Add to Cart: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        haloLabel = new javax.swing.JLabel();
        keranjangBtn = new javax.swing.JButton();
        profileBtn = new javax.swing.JButton();
        logoutBtn = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        searchField = new javax.swing.JTextField();
        searchBtn = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel2 = new javax.swing.JPanel();
        barangBtn = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        haloLabel.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        haloLabel.setForeground(new java.awt.Color(0, 32, 64));
        haloLabel.setText("Hallo ..... !");

        keranjangBtn.setBackground(new java.awt.Color(0, 32, 64));
        keranjangBtn.setForeground(new java.awt.Color(255, 255, 255));
        keranjangBtn.setText("Keranjang");
        keranjangBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                keranjangBtnActionPerformed(evt);
            }
        });

        profileBtn.setBackground(new java.awt.Color(0, 32, 64));
        profileBtn.setForeground(new java.awt.Color(255, 255, 255));
        profileBtn.setText("Profile");
        profileBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                profileBtnActionPerformed(evt);
            }
        });

        logoutBtn.setBackground(new java.awt.Color(0, 32, 64));
        logoutBtn.setForeground(new java.awt.Color(255, 255, 255));
        logoutBtn.setText("Logout");
        logoutBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                logoutBtnActionPerformed(evt);
            }
        });

        jLabel1.setText("Cari nama barang");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 193, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(haloLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(keranjangBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(profileBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(logoutBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(28, 28, 28))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(41, 41, 41)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(haloLabel)
                    .addComponent(keranjangBtn)
                    .addComponent(profileBtn)
                    .addComponent(logoutBtn))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 23, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addContainerGap())
        );

        jPanel3.setBackground(new java.awt.Color(0, 32, 64));

        searchBtn.setText("Search");
        searchBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchBtnActionPerformed(evt);
            }
        });

        jScrollPane1.setBackground(new java.awt.Color(0, 32, 64));
        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        jPanel2.setBackground(new java.awt.Color(0, 32, 64));

        barangBtn.setText("jButton4");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addComponent(barangBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(697, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(barangBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(213, Short.MAX_VALUE))
        );

        jScrollPane1.setViewportView(jPanel2);

        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("240002 - 240036 - 240060");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addComponent(searchField, javax.swing.GroupLayout.PREFERRED_SIZE, 536, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(49, 49, 49)
                .addComponent(searchBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel2)
                .addGap(22, 22, 22))
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 818, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(searchField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(searchBtn))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 341, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel2)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void keranjangBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_keranjangBtnActionPerformed
        FrameKeranjang keranjang = new FrameKeranjang(userEmail);
        keranjang.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_keranjangBtnActionPerformed

    private void profileBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_profileBtnActionPerformed
        FrameProfile profile = new FrameProfile(this.userEmail); 
        profile.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_profileBtnActionPerformed

    private void logoutBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_logoutBtnActionPerformed
        FrameSignIn signIn = new FrameSignIn();
        signIn.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_logoutBtnActionPerformed

    private void searchBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchBtnActionPerformed
        String keyword = searchField.getText();
        
        // 2. Reload the grid with the filter
        loadProduk(keyword);
    }//GEN-LAST:event_searchBtnActionPerformed
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            // CRITICAL: You MUST put a real email from your database here!
            // If your database has "joan@gmail.com", put exactly that.
            new FrameDashboard("joan@gmail.com").setVisible(true); 
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton barangBtn;
    private javax.swing.JLabel haloLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton keranjangBtn;
    private javax.swing.JButton logoutBtn;
    private javax.swing.JButton profileBtn;
    private javax.swing.JButton searchBtn;
    private javax.swing.JTextField searchField;
    // End of variables declaration//GEN-END:variables
}
