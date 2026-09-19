# CalloraX Product Specification

## Brand
- App name: CalloraX
- Tagline: Your Privacy. Your Calls.
- Original premium adaptive launcher icon.
- Black dialer surface with white/light keypad buttons.

## Plans
| Plan | Price | Entitlement |
|---|---:|---|
| 3 Days Pro Free Trial | Free | Pro, no ads, green verified badge |
| 1 Month Pro | Rs. 99 | Pro, no ads, green verified badge |
| 3 Months Pro | Rs. 199 | Pro, no ads, green verified badge |
| 6 Months Pro | Rs. 499 | Pro, no ads, green verified badge |
| 1 Year Premium | Rs. 3,999 | Premium, no ads, gray verified badge |
| Lifetime Access Pro+Premium | Rs. 4,999 | Lifetime Pro + Premium, no ads |

## Manual payments
- Easypaisa IBAN: configured from the project owner's supplied value.
- Binance TRC20 and BEP20 addresses: configured from the project owner's supplied values.
- Screenshot is mandatory.
- Every submission starts as Pending.
- Only authorized admin approval activates an entitlement.
- Receipt storage must remain private.

## Store / Themes
Built-in catalog includes Dark, Light, Midnight, Purple, Blue, Cyan, Violet, Emerald, Rose, Sunset and Mixed Premium. The database model supports future unlimited theme packs and per-user downloads/favorites.

## Social shortcuts
Use Android intents for SMS, WhatsApp, Facebook, Instagram and other installed apps where supported. If unavailable, fail gracefully. Never pretend an app is installed.

## Dialer
Real Android telephony APIs only. Dual SIM uses TelephonyManager/SubscriptionManager where supported. Voice recording must obey Android/device/region legal and technical restrictions.

## Play Store
This project can be prepared for release, but Play Console submission still requires manual signing, store listing, privacy policy/data safety declarations, permission declarations where applicable, testing tracks and policy review. Do not claim automatic compliance.
