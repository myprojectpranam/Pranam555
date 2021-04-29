package notifications;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {

    @Headers({

            "Content-Type:application/json",
            "Authorization:key=AAAAsn0Z8dg:APA91bHIudpSDtWeu62vZJT5khgkpubWFNocHaPHGK0mOqqyy0vjGgdI5ZrPhAnmLrGEQnQkfZ1tpH96LHvtIecV7bxV3ICaz9FSGW4XUB3-U70yd7n2F0Oo0ieJpjXw4vfuHIIvNrir"
    })

    @POST("fcm/send")
    Call<Response> sendNotification(@Body Sender body);

}
