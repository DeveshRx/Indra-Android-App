package devesh.common.ViewModel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.HashMap;
import java.util.List;

import devesh.common.database.contactsdb.ContactUser;


public class AppDataViewModel extends ViewModel {

    // Contacts
    private MutableLiveData<List<ContactUser>> contactsListMain;
    private MutableLiveData<List<ContactUser>> favContactsListMain;
    //App Update
    private MutableLiveData<HashMap<String, String>> ServerBuildConfig;

    public MutableLiveData<List<ContactUser>> getAppContactsList() {


        if (contactsListMain == null) {
            contactsListMain = new MutableLiveData<List<ContactUser>>();
        }

        return contactsListMain;
    }

    public MutableLiveData<List<ContactUser>> getAppFavContactsList() {
        if (favContactsListMain == null) {
            favContactsListMain = new MutableLiveData<List<ContactUser>>();
        }
        return favContactsListMain;
    }

    public MutableLiveData<HashMap<String, String>> getServerBuildConfig() {
        if (ServerBuildConfig == null) {
            ServerBuildConfig = new MutableLiveData<HashMap<String, String>>();
        }
        return ServerBuildConfig;
    }


}