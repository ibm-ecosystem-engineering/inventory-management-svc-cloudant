package com.ibm.inventory_management.services;

import java.io.IOException;
import java.util.List;
import javax.annotation.PostConstruct;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.UUID;

import com.cloudant.client.api.CloudantClient;
import com.cloudant.client.api.ClientBuilder;
import com.cloudant.client.api.Database;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import com.ibm.inventory_management.config.CloudantConfig;
import com.ibm.inventory_management.models.StockItem;

@Service
@Primary
public class StockItemService implements StockItemApi {
  @Bean
  public static CloudantClient buildCloudant(CloudantConfig config) throws CloudServicesException {
    System.out.println("Config: " + config);
    URL url = null;

    try {
      url = new URL(config.getUrl());
    } catch (MalformedURLException e) {
      throw new CloudServicesException("Invalid service URL specified", e);
    }

    return ClientBuilder
        .url(url)
        .iamApiKey(config.getApiKey())
        // .username(config.getUsername())
        // .iamApiKey(config.getPassword())
        .build();
  }

  private CloudantConfig config;
  private CloudantClient client;
  private Database db = null;

  public StockItemService(CloudantConfig config, @Lazy CloudantClient client) {
    this.config = config;
    this.client = client;
  }

  @PostConstruct
  public void init() {
    db = client.database(config.getDatabaseName(), true);
  }

  @Override
  public List<StockItem> listStockItems() throws Exception {

    try {
      return db.getAllDocsRequestBuilder()
          .includeDocs(true)
          .build()
          .getResponse()
          .getDocsAs(StockItem.class);

    } catch (IOException e) {
      throw new Exception("", e);
    }
  }

  @Override
  public void addStockItem(String name, Double price, Integer stock, String manufacturer) throws Exception {
    try {
      db.save(new StockItem(UUID.randomUUID().toString())
              .withName(name)
              .withPrice(price)
              .withStock(stock)
              .withManufacturer(manufacturer)
      );
    } catch (Exception e) {
      throw new Exception("",e);
    }
  }

  @Override
  public void updateStockItem(String id, String name, Double price, Integer stock, String manufacturer) throws Exception {
    try {
      StockItem itemToUpdate = db.find(StockItem.class,id);

      itemToUpdate.setName(name !=null ? name : itemToUpdate.getName());
      itemToUpdate.setManufacturer(manufacturer != null ? manufacturer : itemToUpdate.getManufacturer());
      itemToUpdate.setPrice(price != null ? price : itemToUpdate.getPrice());
      itemToUpdate.setStock(stock != null ? stock : itemToUpdate.getStock());

      db.update(itemToUpdate);
    } catch (Exception e ){
      throw new Exception("", e);
    }
  }

  @Override
  public void deleteStockItem(String id) throws Exception {
    try {
      db.remove(db.find(StockItem.class,id));
    } catch (Exception e){
      throw new Exception("",e);
    }
  }
}
