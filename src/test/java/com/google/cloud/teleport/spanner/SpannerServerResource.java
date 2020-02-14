/*
 * Copyright (C) 2020 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.cloud.teleport.spanner;

import com.google.cloud.spanner.BatchClient;
import com.google.cloud.spanner.DatabaseAdminClient;
import com.google.cloud.spanner.DatabaseId;
import com.google.cloud.spanner.Spanner;
import com.google.cloud.spanner.SpannerException;
import com.google.cloud.spanner.SpannerOptions;
import org.junit.rules.ExternalResource;

/** Facilitates setup and deletion of a Spanner database for integration tests. */
public class SpannerServerResource extends ExternalResource {
  // Modify the following parameters to match your Cloud Spanner instance.
  private final String projectId = "test-project";
  private final String instanceId = "test-instance";
  private final String host = "https://spanner.googleapis.com";

  private Spanner client;
  private DatabaseAdminClient databaseAdminClient;

  @Override
  protected void before() throws Throwable {
    SpannerOptions spannerOptions =
        SpannerOptions.newBuilder().setProjectId(projectId).setHost(host).build();
    client = spannerOptions.getService();
    databaseAdminClient = client.getDatabaseAdminClient();
  }

  @Override
  protected void after() {
    client.close();
  }

  public void createDatabase(String dbName, Iterable<String> ddlStatements) throws Exception {
    // Waits for create database to complete.
    databaseAdminClient.createDatabase(instanceId, dbName, ddlStatements).get();
  }

  public void dropDatabase(String dbName) {
    try {
      databaseAdminClient.dropDatabase(instanceId, dbName);
    } catch (SpannerException e) {
      // Does not exist, ignore.
    }
  }

  public BatchClient getBatchClient(String dbName) {
    return client.getBatchClient(DatabaseId.of(projectId, instanceId, dbName));
  }
}
