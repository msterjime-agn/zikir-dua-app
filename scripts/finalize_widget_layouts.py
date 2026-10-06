from pathlib import Path
import xml.etree.ElementTree as ET
r=Path('app/src/main/res')
xml='''<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android" android:id="@+id/widget_root" android:layout_width="match_parent" android:layout_height="match_parent" android:orientation="horizontal" android:gravity="center_vertical" android:padding="12dp" android:background="@drawable/widget_prayer_dark">
<LinearLayout android:layout_width="0dp" android:layout_height="wrap_content" android:layout_weight="1" android:orientation="vertical">
 <TextView android:id="@+id/widget_city" android:layout_width="match_parent" android:layout_height="wrap_content" android:text="NAMAZ WAGTY" android:textSize="14sp" android:textColor="#FFFFFF" android:textStyle="bold" android:maxLines="1" android:ellipsize="end"/>
 <TextView android:id="@+id/widget_next" android:layout_width="match_parent" android:layout_height="wrap_content" android:text="—" android:textSize="12sp" android:textColor="#E8CD85" android:maxLines="1" android:ellipsize="end"/>
 <TextView android:id="@+id/widget_meta" android:layout_width="match_parent" android:layout_height="wrap_content" android:text="—" android:textSize="10sp" android:textColor="#D1E4DB" android:maxLines="1" android:ellipsize="end"/>
</LinearLayout>
<TextView android:id="@+id/widget_time" android:layout_width="wrap_content" android:layout_height="wrap_content" android:layout_marginStart="6dp" android:text="--:--" android:textSize="24sp" android:textStyle="bold" android:textColor="#FFFFFF"/>
<TextView android:id="@+id/widget_refresh" android:layout_width="48dp" android:layout_height="48dp" android:text="↻" android:textSize="27sp" android:textColor="#E8CD85" android:gravity="center" android:contentDescription="Refresh"/>
</LinearLayout>'''
ET.fromstring(xml)
(r/'layout/widget_prayer_compact.xml').write_text(xml,encoding='utf-8')
p=r/'xml/widget_prayer_compact_info.xml';s=p.read_text().replace('72dp','80dp');p.write_text(s)
p=r/'xml/widget_prayer_full_info.xml';s=p.read_text().replace('180dp','208dp');p.write_text(s)
p=Path('app/build.gradle');s=p.read_text()
if 'NAMAZ_KEYSTORE' not in s:
 s=s.replace('    buildFeatures {','''    // Optional release signing, supplied locally. NEVER commit a private keystore.
    if (System.getenv('NAMAZ_KEYSTORE')) {
        signingConfigs {
            standalone {
                storeFile file(System.getenv('NAMAZ_KEYSTORE'))
                storePassword System.getenv('NAMAZ_STORE_PASSWORD')
                keyAlias 'namaz-wagty'
                keyPassword System.getenv('NAMAZ_STORE_PASSWORD')
            }
        }
        buildTypes { release { signingConfig signingConfigs.standalone } }
    }

    buildFeatures {''')
 p.write_text(s)
print('Compact widget layout and external signing configuration ready')
