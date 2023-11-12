 package com.example.emmazon;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

 public class MainActivity extends AppCompatActivity {

     DatabaseReference databaseProducts;
     EditText editTextName;
     EditText editTextPrice;
     Button buttonAddProduct;
     ListView listViewProducts;

     List<Product> products;

     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         databaseProducts = FirebaseDatabase.getInstance().getReference("products");

             editTextName = (EditText) findViewById(R.id.editTextName);
         editTextPrice = (EditText) findViewById(R.id.editTextPrice);
         listViewProducts = (ListView) findViewById(R.id.listViewProducts);
         buttonAddProduct = (Button) findViewById(R.id.addButton);

         products = new ArrayList<>();

         //adding an onclicklistener to button
         buttonAddProduct.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 addProduct();
             }
         });

         listViewProducts.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
             @Override
             public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                 Product product = products.get(i);
                 showUpdateDeleteDialog(product.getId(), product.getProductName());
                 return true;
             }
         });
     }


     @Override
     protected void onStart() {
         super.onStart();
         //attaching value event listener
         databaseProducts.addValueEventListener(new ValueEventListener() {
             @Override
             public void onDataChange(@NonNull DataSnapshot snapshot) {
                 //clearing the previous product list
                 products.clear();

                 //iterating through all the nodes, refreshes the app
                 for(DataSnapshot postSnapshot : snapshot.getChildren()){
                     //get product
                     Product prod = postSnapshot.getValue(Product.class);
                     //add to the list
                     products.add(prod);
                 }

                 //create adapter
                 ProductList productsAdapter = new ProductList(MainActivity. this, products);
                 //attach the adapter to the listView
                 listViewProducts.setAdapter(productsAdapter);
             }

             @Override
             public void onCancelled(@NonNull DatabaseError error) {

             }
         });
     }


     private void showUpdateDeleteDialog(final String productId, String productName) {

         AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
         LayoutInflater inflater = getLayoutInflater();
         final View dialogView = inflater.inflate(R.layout.update_dialog, null);
         dialogBuilder.setView(dialogView);

         final EditText editTextName = (EditText) dialogView.findViewById(R.id.editTextName);
         final EditText editTextPrice  = (EditText) dialogView.findViewById(R.id.editTextPrice);
         final Button buttonUpdate = (Button) dialogView.findViewById(R.id.buttonUpdateProduct);
         final Button buttonDelete = (Button) dialogView.findViewById(R.id.buttonDeleteProduct);

         dialogBuilder.setTitle(productName);
         final AlertDialog b = dialogBuilder.create();
         b.show();

         buttonUpdate.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 String name = editTextName.getText().toString().trim();
                 double price = Double.parseDouble(String.valueOf(editTextPrice.getText().toString()));
                 if (!TextUtils.isEmpty(name)) {
                     updateProduct(productId, name, price);
                     b.dismiss();
                 }
             }
         });

         buttonDelete.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 deleteProduct(productId);
                 b.dismiss();
             }
         });
     }

     private void updateProduct(String id, String name, double price) {
         //get reference
         DatabaseReference dR = FirebaseDatabase.getInstance().getReference("products").child(id);

         //updating product
         Product product = new Product(id, name, price);
         dR.setValue(product);

         Toast.makeText(getApplicationContext(), "product updated", Toast.LENGTH_LONG).show();
     }

     private boolean deleteProduct(String id) {
         // get the reference
         DatabaseReference dR = FirebaseDatabase.getInstance().getReference("products").child(id);

         //remove the id
         dR.removeValue();
         Toast.makeText(getApplicationContext(), "product deleted", Toast.LENGTH_LONG).show();
         return true;
     }

     private void addProduct() {
         String name = editTextName.getText().toString().trim();
         double price = Double.parseDouble(String.valueOf(editTextPrice.getText().toString()));
         //check for values
         if(!TextUtils.isEmpty(name)){
             //if there is something in the name editText
             //getting a unique id using push().getKey() method
             String id = databaseProducts.push().getKey();

             //creating a Product obj
             Product prod = new Product(id, name, price);

             //send it to the db
             databaseProducts.child(id).setValue(prod);

             //reset the editTexts
             editTextName.setText("");
             editTextPrice.setText("");

             //display a success toast
             Toast.makeText(this, "product added", Toast.LENGTH_LONG).show();
         } else {
             //if the value is not given display toast
             Toast.makeText(this, "please enter a name", Toast.LENGTH_LONG).show();
         }

     }
 }