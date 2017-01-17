package com.lawrey.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.lawrey.inventoryapp.data.InventoryContract;

import static android.R.attr.id;

/**
 * Created by Lawrey on 4/1/17.
 */

public class InventoryCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link InventoryCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the pet data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current pet can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        // Find individual views that we want to modify in the list item layout
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView quantityTextView = (TextView) view.findViewById(R.id.quantity);
        TextView priceTextView = (TextView) view.findViewById(R.id.price);
        ImageView imageView = (ImageView) view.findViewById(R.id.imageView_item);

        // Find the columns of pet attributes that we're interested in
        int nameColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_INVENTORY_NAME);
        int quantityColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_INVENTORY_QUANTITY);
        int priceColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_INVENTORY_PRICE);
        int imageColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_INVENTORY_IMAGE);



        // Read the pet attributes from the Cursor for the current inventory
        final String inventoryItemName = cursor.getString(nameColumnIndex);
        int inventoryItemQuantity = cursor.getInt(quantityColumnIndex);
        double inventoryItemPrice = cursor.getDouble(priceColumnIndex);
        byte[] inventoryImage = cursor.getBlob(imageColumnIndex);

        // Update the TextViews with the attributes for the current inventory
        nameTextView.setText(inventoryItemName);
        quantityTextView.setText("Remaining Stock : " + inventoryItemQuantity);
        priceTextView.setText("Price : $" + String.format("%.2f", inventoryItemPrice));
        imageView.setImageBitmap(getImage(inventoryImage));

        Button onOrderMoreButton = (Button) view.findViewById(R.id.button_order_more);

        onOrderMoreButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("message/rfc822");
                i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"orders@udacitytesting.com"});
                i.putExtra(Intent.EXTRA_SUBJECT, "Order more " + inventoryItemName);
                i.putExtra(Intent.EXTRA_TEXT   , "Put in your requirements here");
                try {
                    context.startActivity(Intent.createChooser(i, "Send mail..."));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(context, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        int inventoryIDColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry._ID);
        int inventoryID = cursor.getInt(inventoryIDColumnIndex);

        Button onSaleButton = (Button) view.findViewById(R.id.button_sale);

        final Uri currentInventoryUri = ContentUris.withAppendedId(InventoryContract.InventoryEntry.CONTENT_URI, inventoryID);

        final ContentValues values = new ContentValues();
        values.put(InventoryContract.InventoryEntry.COLUMN_INVENTORY_NAME, inventoryItemName);
        values.put(InventoryContract.InventoryEntry.COLUMN_INVENTORY_PRICE, inventoryItemPrice);

        final int adjustedItemQuantity = inventoryItemQuantity - 1;

        onSaleButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                if (adjustedItemQuantity >= 0) {
                    values.put(InventoryContract.InventoryEntry.COLUMN_INVENTORY_QUANTITY, adjustedItemQuantity);
                    int rowsAffected = context.getContentResolver().update(currentInventoryUri, values, null, null);
                    Log.v("rowsAffected", "" + rowsAffected);
                }else{
                    Toast.makeText(context, "No more stock for sale.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // convert from byte array to bitmap
    public static Bitmap getImage(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }
}
