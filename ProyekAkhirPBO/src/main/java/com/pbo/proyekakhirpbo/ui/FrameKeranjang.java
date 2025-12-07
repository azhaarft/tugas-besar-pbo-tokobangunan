/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.pbo.proyekakhirpbo.ui;
import com.pbo.proyekakhirpbo.db.Konektor; 
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JCheckBox;
/**
 *
 * @author acer
 */
public class FrameKeranjang extends javax.swing.JFrame {
    
    private List<CartItem> cartList = new ArrayList<>();
    private String userEmail; 
    private int userID; 
    private long totalBelanja = 0;
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(FrameKeranjang.class.getName());

    /**
     * Creates new form FrameKeranjang
     */
    public FrameKeranjang(String email) {
        this.userEmail = email;
        initComponents();
        
        jPanel3.setLayout(new BoxLayout(jPanel3, BoxLayout.Y_AXIS));
        
        getUserId(); 
        loadKeranjang(); 
    }
    
    private class CartItem {
        int idKeranjang;
        int idProduk;
        double harga;
        int qty;
        JCheckBox checkBox;

        public CartItem(int idK, int idP, double h, int q, JCheckBox cb) {
            this.idKeranjang = idK;
            this.idProduk = idP;
            this.harga = h;
            this.qty = q;
            this.checkBox = cb;
        }
        
        public double getSubtotal() {
            return harga * qty;
        }
    }
    
    private void processCheckout() {
        List<CartItem> selectedItems = new ArrayList<>();
        long checkoutTotal = 0;

        for (CartItem item : cartList) {
            if (item.checkBox.isSelected()) {
                selectedItems.add(item);
                checkoutTotal += item.getSubtotal();
            }
        }

        if (selectedItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tidak ada barang yang dipilih!");
            return;
        }

        Connection conn = null;
        try {
            conn = Konektor.getConnection();
            conn.setAutoCommit(false); 

            String sqlTrans = "INSERT INTO transaksi (id_user, total, status) VALUES (?, ?, 'Pending')";
            PreparedStatement pstTrans = conn.prepareStatement(sqlTrans, Statement.RETURN_GENERATED_KEYS);
            pstTrans.setInt(1, userID);
            pstTrans.setDouble(2, checkoutTotal);
            pstTrans.executeUpdate();
            
            ResultSet rsID = pstTrans.getGeneratedKeys();
            int idTransaksiBaru = 0;
            if (rsID.next()) {
                idTransaksiBaru = rsID.getInt(1);
            }

            String sqlDetail = "INSERT INTO detail_transaksi (id_transaksi, id_produk, kuantitas, harga) VALUES (?, ?, ?, ?)";
            PreparedStatement pstDetail = conn.prepareStatement(sqlDetail);

            String sqlDeleteCart = "DELETE FROM keranjang WHERE id_keranjang = ?";
            PreparedStatement pstDelete = conn.prepareStatement(sqlDeleteCart);

            for (CartItem item : selectedItems) {
                pstDetail.setInt(1, idTransaksiBaru);
                pstDetail.setInt(2, item.idProduk);
                pstDetail.setInt(3, item.qty);
                pstDetail.setDouble(4, item.harga);
                pstDetail.executeUpdate();
                
                pstDelete.setInt(1, item.idKeranjang);
                pstDelete.executeUpdate();
            }

            conn.commit(); 
            JOptionPane.showMessageDialog(this, "Checkout Berhasil! ID: " + idTransaksiBaru);
            
            loadKeranjang(); 

        } catch (Exception e) {
            try { if (conn != null) conn.rollback(); } catch (Exception ex) {}
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Checkout Gagal: " + e.getMessage());
        }
    }
    
    public FrameKeranjang() {
        initComponents();
    }
    
    private void getUserId() {
        try {
            Connection conn = Konektor.getConnection();
            String sql = "SELECT id_user, nama FROM user WHERE email = ?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, userEmail);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                this.userID = rs.getInt("id_user");
                System.out.println("DEBUG: Found User ID = " + this.userID); 
                jLabel1.setText("Keranjang " + rs.getString("nama"));
            } else {
                System.out.println("DEBUG: User NOT found for email: " + userEmail);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void loadKeranjang() {
        jPanel3.removeAll();
        totalBelanja = 0;

        try {
            Connection conn = Konektor.getConnection();
            
            String sql = "SELECT k.id_keranjang, k.id_produk, k.kuantitas, p.nama_barang, p.harga_barang " +
                         "FROM keranjang k " +
                         "JOIN produk p ON k.id_produk = p.id_produk " +
                         "WHERE k.id_user = ?";
                         
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, userID);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                int idKeranjang = rs.getInt("id_keranjang");
                int idProduk = rs.getInt("id_produk"); // We need this now
                String nama = rs.getString("nama_barang");
                double harga = rs.getDouble("harga_barang");
                int qty = rs.getInt("kuantitas");
                
                JPanel rowPanel = new JPanel();
                rowPanel.setPreferredSize(new Dimension(600, 100)); 
                rowPanel.setMaximumSize(new Dimension(1000, 100)); 
                rowPanel.setBackground(new Color(240, 240, 240));
                rowPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));
                rowPanel.setLayout(null); 

                JCheckBox chk = new JCheckBox();
                chk.setBounds(10, 35, 20, 20);
                chk.setSelected(true);
                
                chk.addActionListener(e -> calculateTotalDisplay());
                
                rowPanel.add(chk);

                cartList.add(new CartItem(idKeranjang, idProduk, harga, qty, chk));

                JLabel lblName = new JLabel(nama);
                lblName.setFont(new Font("Segoe UI", Font.BOLD, 14));
                lblName.setBounds(40, 20, 200, 20);
                rowPanel.add(lblName);

                JLabel lblPrice = new JLabel("Rp " + (long)harga);
                lblPrice.setBounds(40, 45, 100, 20);
                rowPanel.add(lblPrice);

                JButton btnMin = new JButton("-");
                btnMin.setBounds(300, 30, 40, 30);
                btnMin.addActionListener(e -> updateQty(idKeranjang, qty - 1));
                rowPanel.add(btnMin);

                JTextField txtQty = new JTextField(String.valueOf(qty));
                txtQty.setHorizontalAlignment(JTextField.CENTER);
                txtQty.setEditable(false);
                txtQty.setBounds(345, 30, 40, 30);
                rowPanel.add(txtQty);

                JButton btnPlus = new JButton("+");
                btnPlus.setBounds(390, 30, 40, 30);
                btnPlus.addActionListener(e -> updateQty(idKeranjang, qty + 1));
                rowPanel.add(btnPlus);

                JButton btnDel = new JButton("Delete");
                btnDel.setBackground(Color.RED); 
                btnDel.setForeground(Color.WHITE);
                btnDel.setBounds(440, 30, 70, 30);
                btnDel.addActionListener(e -> deleteItem(idKeranjang));
                rowPanel.add(btnDel);

                jPanel3.add(rowPanel);
                jPanel3.add(javax.swing.Box.createRigidArea(new Dimension(0, 10)));
            }
            calculateTotalDisplay();

            jPanel3.revalidate();
            jPanel3.repaint();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void calculateTotalDisplay() {
        long currentTotal = 0;
        for (CartItem item : cartList) {
            if (item.checkBox.isSelected()) {
                currentTotal += item.getSubtotal();
            }
        }
        totalLabel.setText("Total : Rp " + currentTotal);
    }
  
    private void updateQty(int idKeranjang, int newQty) {
        if (newQty < 1) return;
        
        try {
            Connection conn = Konektor.getConnection();
            String sql = "UPDATE keranjang SET kuantitas = ? WHERE id_keranjang = ?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, newQty);
            pst.setInt(2, idKeranjang);
            pst.executeUpdate();
            
            loadKeranjang();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteItem(int idKeranjang) {
        int confirm = JOptionPane.showConfirmDialog(this, "Hapus barang ini?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                Connection conn = Konektor.getConnection();
                String sql = "DELETE FROM keranjang WHERE id_keranjang = ?";
                PreparedStatement pst = conn.prepareStatement(sql);
                pst.setInt(1, idKeranjang);
                pst.executeUpdate();
                
                loadKeranjang();
            } catch (Exception e) {
                e.printStackTrace();
            }
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
        jLabel1 = new javax.swing.JLabel();
        logoutBtn = new javax.swing.JButton();
        profileBtn = new javax.swing.JButton();
        dashboardBtn = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel3 = new javax.swing.JPanel();
        jTextField1 = new javax.swing.JTextField();
        searchBtn = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        totalLabel = new javax.swing.JLabel();
        checkoutBtn = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(0, 32, 64));
        jLabel1.setText("Hallo ..... !");

        logoutBtn.setBackground(new java.awt.Color(0, 32, 64));
        logoutBtn.setForeground(new java.awt.Color(255, 255, 255));
        logoutBtn.setText("Logout");
        logoutBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                logoutBtnActionPerformed(evt);
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

        dashboardBtn.setBackground(new java.awt.Color(0, 32, 64));
        dashboardBtn.setForeground(new java.awt.Color(255, 255, 255));
        dashboardBtn.setText("Dashboard");
        dashboardBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dashboardBtnActionPerformed(evt);
            }
        });

        jLabel2.setText("Cari nama barang");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 182, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(dashboardBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(profileBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, 12)
                        .addComponent(logoutBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(36, 36, 36))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(36, 36, 36)
                        .addComponent(jLabel1))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(26, 26, 26)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(logoutBtn)
                            .addComponent(profileBtn)
                            .addComponent(dashboardBtn))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 26, Short.MAX_VALUE)
                .addComponent(jLabel2)
                .addContainerGap())
        );

        jPanel2.setBackground(new java.awt.Color(0, 32, 64));

        jPanel3.setBackground(new java.awt.Color(0, 32, 64));

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 667, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 355, Short.MAX_VALUE)
        );

        jScrollPane1.setViewportView(jPanel3);

        searchBtn.setText("Search");
        searchBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchBtnActionPerformed(evt);
            }
        });

        jLabel6.setForeground(new java.awt.Color(255, 255, 255));
        jLabel6.setText("240002 - 240036 - 240060");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 314, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(37, 37, 37)
                .addComponent(searchBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(97, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 144, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(59, 59, 59))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 521, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(searchBtn))
                .addGap(31, 31, 31)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 335, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel6)
                .addContainerGap(12, Short.MAX_VALUE))
        );

        jPanel4.setBackground(new java.awt.Color(255, 255, 255));

        totalLabel.setText("Total :");

        checkoutBtn.setBackground(new java.awt.Color(0, 32, 64));
        checkoutBtn.setForeground(new java.awt.Color(255, 255, 255));
        checkoutBtn.setText("Checkout");
        checkoutBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkoutBtnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap(20, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(totalLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 206, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(checkoutBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(21, 21, 21))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addComponent(totalLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(checkoutBtn)
                .addGap(18, 18, 18))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void logoutBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_logoutBtnActionPerformed
        FrameSignIn login = new FrameSignIn();
        login.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_logoutBtnActionPerformed

    private void searchBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchBtnActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_searchBtnActionPerformed

    private void dashboardBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dashboardBtnActionPerformed
        FrameDashboard dashboard = new FrameDashboard(this.userEmail);
        dashboard.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_dashboardBtnActionPerformed

    private void profileBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_profileBtnActionPerformed
        FrameProfile profile = new FrameProfile(this.userEmail);
        profile.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_profileBtnActionPerformed

    private void checkoutBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkoutBtnActionPerformed
        processCheckout();
    }//GEN-LAST:event_checkoutBtnActionPerformed

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
        
        java.awt.EventQueue.invokeLater(() -> new FrameKeranjang().setVisible(true));

    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton checkoutBtn;
    private javax.swing.JButton dashboardBtn;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JButton logoutBtn;
    private javax.swing.JButton profileBtn;
    private javax.swing.JButton searchBtn;
    private javax.swing.JLabel totalLabel;
    // End of variables declaration//GEN-END:variables
}
