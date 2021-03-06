package org.mariotaku.twidere.extension

import android.accounts.Account
import android.accounts.AccountManager
import android.support.annotation.ColorInt
import com.bluelinelabs.logansquare.LoganSquare
import org.mariotaku.ktextension.toInt
import org.mariotaku.twidere.TwidereConstants.*
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.account.AccountExtras
import org.mariotaku.twidere.model.account.StatusNetAccountExtras
import org.mariotaku.twidere.model.account.TwitterAccountExtras
import org.mariotaku.twidere.model.account.cred.BasicCredentials
import org.mariotaku.twidere.model.account.cred.Credentials
import org.mariotaku.twidere.model.account.cred.EmptyCredentials
import org.mariotaku.twidere.model.account.cred.OAuthCredentials
import org.mariotaku.twidere.util.ParseUtils
import org.mariotaku.twidere.util.toHexColor


fun Account.getCredentials(am: AccountManager): Credentials {
    val authToken = am.peekAuthToken(this, ACCOUNT_AUTH_TOKEN_TYPE) ?: run {
        throw IllegalStateException("AuthToken is null for ${this}")
    }
    return parseCredentials(authToken, getCredentialsType(am))
}

@Credentials.Type
fun Account.getCredentialsType(am: AccountManager): String {
    return am.getUserData(this, ACCOUNT_USER_DATA_CREDS_TYPE) ?: Credentials.Type.OAUTH
}

fun Account.getAccountKey(am: AccountManager): UserKey {
    val accountKeyString = am.getUserData(this, ACCOUNT_USER_DATA_KEY) ?: run {
        throw IllegalStateException("UserKey is null for ${this}")
    }
    return UserKey.valueOf(accountKeyString)
}

fun Account.setAccountKey(am: AccountManager, accountKey: UserKey) {
    am.setUserData(this, ACCOUNT_USER_DATA_KEY, accountKey.toString())
}

fun Account.getAccountUser(am: AccountManager): ParcelableUser {
    val user = LoganSquare.parse(am.getUserData(this, ACCOUNT_USER_DATA_USER), ParcelableUser::class.java)
    user.is_cache = true
    return user
}

fun Account.setAccountUser(am: AccountManager, user: ParcelableUser) {
    am.setUserData(this, ACCOUNT_USER_DATA_USER, LoganSquare.serialize(user))
}

@ColorInt
fun Account.getColor(am: AccountManager): Int {
    return ParseUtils.parseColor(am.getUserData(this, ACCOUNT_USER_DATA_COLOR), 0)
}

fun Account.getPosition(am: AccountManager): Int {
    return am.getUserData(this, ACCOUNT_USER_DATA_POSITION).toInt(-1)
}

fun Account.getAccountExtras(am: AccountManager): AccountExtras? {
    val json = am.getUserData(this, ACCOUNT_USER_DATA_EXTRAS) ?: return null
    when (getAccountType(am)) {
        AccountType.TWITTER -> {
            return LoganSquare.parse(json, TwitterAccountExtras::class.java)
        }
        AccountType.STATUSNET -> {
            return LoganSquare.parse(json, StatusNetAccountExtras::class.java)
        }
    }
    return null
}

@AccountType
fun Account.getAccountType(am: AccountManager): String {
    return am.getUserData(this, ACCOUNT_USER_DATA_TYPE) ?: AccountType.TWITTER
}

fun Account.isActivated(am: AccountManager): Boolean {
    return am.getUserData(this, ACCOUNT_USER_DATA_ACTIVATED).orEmpty().toBoolean()
}

fun Account.setActivated(am: AccountManager, activated: Boolean) {
    am.setUserData(this, ACCOUNT_USER_DATA_ACTIVATED, activated.toString())
}

fun Account.setColor(am: AccountManager, color: Int) {
    am.setUserData(this, ACCOUNT_USER_DATA_COLOR, toHexColor(color))
}

fun Account.setPosition(am: AccountManager, position: Int) {
    am.setUserData(this, ACCOUNT_USER_DATA_POSITION, position.toString())
}


private fun parseCredentials(authToken: String, @Credentials.Type authType: String): Credentials {
    when (authType) {
        Credentials.Type.OAUTH, Credentials.Type.XAUTH -> return LoganSquare.parse(authToken, OAuthCredentials::class.java)
        Credentials.Type.BASIC -> return LoganSquare.parse(authToken, BasicCredentials::class.java)
        Credentials.Type.EMPTY -> return LoganSquare.parse(authToken, EmptyCredentials::class.java)
    }
    throw UnsupportedOperationException()
}