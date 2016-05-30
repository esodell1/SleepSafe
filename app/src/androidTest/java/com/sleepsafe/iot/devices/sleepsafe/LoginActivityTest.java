package com.sleepsafe.iot.devices.sleepsafe;

import android.test.ActivityInstrumentationTestCase2;

import com.robotium.solo.Solo;
import com.sleepsafe.iot.devices.sleepsafe.activities.LoginActivity;

/**
 * Created by Student on 5/29/2016.
 */
public class LoginActivityTest extends ActivityInstrumentationTestCase2<LoginActivity>{

    private Solo solo;

    public LoginActivityTest() {
        super(LoginActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        solo = new Solo(getInstrumentation(), getActivity());
    }

    @Override
    public void tearDown() throws Exception {
        //tearDown() is run after a test case has finished.
        //finishOpenedActivities() will finish all the activities that have been opened during the test execution.
        solo.finishOpenedActivities();

    }

    public void testLoginActivityShowedUp() {

        boolean textFound = solo.searchText("Login");
        assertTrue("Login activity loaded", textFound);
    }

    public void testRegisterActivityWithExistingEmail() {
        boolean textFound = solo.searchText("Register");
        assertTrue("Login fragment loaded", textFound);
        solo.enterText(0, "testEmail@uw.edu");
        solo.enterText(1, "123456789");
        solo.clickOnButton("Register");
        boolean worked = solo.searchText("SleepSafe");
        assertFalse("Register didn't work!", worked);
    }

    public void testRegisterActivityWithNewEmail() {
        boolean textFound = solo.searchText("Register");
        assertTrue("Login fragment loaded", textFound);
        solo.enterText(0, "testEmail@uw.edu");
        solo.enterText(1, "123456789");
        solo.clickOnButton("Register");
        boolean worked = solo.searchText("SleepSafe");
        assertTrue("Register worked!", worked);
    }

    public void testLoginActivity() {
        boolean textFound = solo.searchText("Login");
        assertTrue("Login activity loaded", textFound);
        solo.enterText(0, "testEmail@uw.edu");
        solo.enterText(1, "123456789");
        solo.clickOnButton("Login");
        boolean worked = solo.searchText("SleepSafe");
        assertTrue("Log In worked!", worked);
    }

    public void testGuestSignInActivity() {
        boolean fragmentLoaded = solo.searchText("Guest");
        assertTrue("Login activity loaded", fragmentLoaded);
        solo.clickOnButton("Guest Sign In");
        boolean worked = solo.searchText("SleepSafe");
        assertTrue("Guest mode worked!", worked);
    }
}
