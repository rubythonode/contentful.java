package com.contentful.java.api;

import com.contentful.java.model.CDAResource;
import com.contentful.java.model.CDASpace;
import com.contentful.java.model.CDASyncedSpace;
import retrofit.RetrofitError;
import retrofit.client.Response;

import java.util.ArrayList;

import static com.contentful.java.lib.Constants.CDAResourceType;

/**
 * TBD
 */
class MergeSpacesRunnable implements Runnable {
    private final CDASyncedSpace originalSpace;
    private final CDASyncedSpace updatedSpace;
    private CDACallback<CDASyncedSpace> callback;
    private Response response;
    private CDASpace space;

    public MergeSpacesRunnable(CDASyncedSpace originalSpace,
                               CDASyncedSpace updatedSpace,
                               CDACallback<CDASyncedSpace> callback,
                               Response response, CDASpace space) {

        this.originalSpace = originalSpace;
        this.updatedSpace = updatedSpace;
        this.callback = callback;
        this.response = response;
        this.space = space;
    }

    @Override
    public void run() {
        ArrayList<CDAResource> originalItems = new ArrayList<CDAResource>(originalSpace.getItems());
        ArrayList<CDAResource> updatedItems = updatedSpace.getItems();

        for (int i = updatedItems.size() - 1; i >= 0; i--) {
            CDAResource item = updatedItems.get(i);
            CDAResourceType resourceType = CDAResourceType.valueOf((String) item.getSys().get("type"));

            if (CDAResourceType.DeletedAsset.equals(resourceType)) {
                item.getSys().put("type", CDAResourceType.Asset.toString());
                originalItems.remove(item);
            } else if (CDAResourceType.DeletedEntry.equals(resourceType)) {
                item.getSys().put("type", CDAResourceType.Entry.toString());
                originalItems.remove(item);
            } else if (CDAResourceType.Asset.equals(resourceType) ||
                    CDAResourceType.Entry.equals(resourceType)) {

                originalItems.remove(item);
                originalItems.add(0, item);
            }
        }

        updatedItems.clear();
        updatedItems.addAll(originalItems);

        new ArrayParserRunnable<CDASyncedSpace>(updatedSpace, new CDACallback<CDASyncedSpace>() {
            @Override
            protected void onSuccess(CDASyncedSpace syncedSpace, Response response) {
                if (!callback.isCancelled()) {
                    callback.success(syncedSpace, response);
                }
            }

            @Override
            protected void onFailure(RetrofitError retrofitError) {
                super.onFailure(retrofitError);

                if (!callback.isCancelled()) {
                    callback.onFailure(retrofitError);
                }
            }
        }, space, response).run();
    }
}