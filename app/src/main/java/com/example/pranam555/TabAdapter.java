package com.example.pranam555;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.pranam555.ui.NewGroupListFragment;

public class TabAdapter extends FragmentPagerAdapter {

    public TabAdapter(FragmentManager fm){

        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int tabPosition) {

        switch (tabPosition){

            case 0:
                ChatsFragment chats = new ChatsFragment();
                return chats;
//
//            case 1:
//                GoupsFragments goupsFragments = new GoupsFragments();
//                return goupsFragments;

            case 1:
                NewGroupListFragment goupsChatFragments = new NewGroupListFragment();
                return goupsChatFragments;

//            case 2:
//                Contacts contacts = new Contacts();
//                return contacts;

//            case 2:
//                RequestFragments requestFragments = new RequestFragments();
//                return requestFragments;

            case 2:
                PhoneBookFragment phoneBookFragment = new PhoneBookFragment();
                return phoneBookFragment;

            default:
                return null;
        }


    }

    @Override
    public int getCount() {
        return 3;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {

        switch (position){

            case 0:
                return "Chats";

            case 1:
                return "Groups";

//            case 2:
//                return "Contacts";
//
//            case 3:
//                return "Requests";

            case 2:
                return "Contacts";

            default:
                return null;
        }



    }
}
