package dev.nachwahl.polymap.util;

import com.algolia.search.DefaultSearchClient;
import com.algolia.search.SearchClient;
import com.algolia.search.SearchIndex;

public class AlgoliaClient {
    private SearchClient client;
    private SearchIndex<Region> regionIndex;
    public AlgoliaClient() {
        FileBuilder fb = new FileBuilder("plugins/PolyMap", "config.yml");
        this.client = DefaultSearchClient.create(fb.getString("algolia.appid"), fb.getString("algolia.apikey"));
        this.regionIndex = client.initIndex(fb.getString("algolia.index"), Region.class);
    }

    public void createRegion(Region region) {
        regionIndex.saveObjectAsync(region);
    }

}
