/**
 * Copyright (c) www.longdw.com
 */
package com.ldw.music.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.Albums;
import android.provider.MediaStore.Audio.Media;
import android.provider.MediaStore.Files.FileColumns;
import android.text.TextUtils;

import com.ldw.music.activity.IConstants;
import com.ldw.music.db.AlbumInfoDao;
import com.ldw.music.db.ArtistInfoDao;
import com.ldw.music.db.FavoriteInfoDao;
import com.ldw.music.db.FolderInfoDao;
import com.ldw.music.db.MusicInfoDao;
import com.ldw.music.model.AlbumInfo;
import com.ldw.music.model.ArtistInfo;
import com.ldw.music.model.FolderInfo;
import com.ldw.music.model.MusicInfo;
import com.ldw.music.storage.SPStorage;

/**
 * 查询各主页信息，获取封面图片等
 * @author longdw(longdawei1988@gmail.com)
 *
 */
public class MusicUtils implements IConstants {

	private static String[] proj_music = new String[] {
			MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE,
			MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.ALBUM_ID,
			MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ARTIST_ID,
			MediaStore.Audio.Media.DURATION };

	private static String[] proj_album = new String[] { Albums.ALBUM,
			Albums.NUMBER_OF_SONGS, Albums._ID, Albums.ALBUM_ART };

	private static String[] proj_artist = new String[] {
			MediaStore.Audio.Artists.ARTIST,
			MediaStore.Audio.Artists.NUMBER_OF_TRACKS };

	private static String[] proj_folder = new String[] { FileColumns.DATA };

	public static final int FILTER_SIZE = 1 * 1024 * 1024;// 1MB
	public static final int FILTER_DURATION = 1 * 60 * 1000;// 1分钟
	private static final BitmapFactory.Options sBitmapOptionsCache = new BitmapFactory.Options();
	private static final BitmapFactory.Options sBitmapOptions = new BitmapFactory.Options();
	private static final HashMap<Long, Bitmap> sArtCache = new HashMap<Long, Bitmap>();
	private static final Uri sArtworkUri = Uri
			.parse("content://media/external/audio/albumart");

	static {
		// for the cache,
		// 565 is faster to decode and display
		// and we don't want to dither here because the image will be scaled
		// down later
		sBitmapOptionsCache.inPreferredConfig = Bitmap.Config.RGB_565;
		sBitmapOptionsCache.inDither = false;

		sBitmapOptions.inPreferredConfig = Bitmap.Config.RGB_565;
		sBitmapOptions.inDither = false;
	}

	// 歌曲信息数据库
	private static MusicInfoDao mMusicInfoDao;
	// 专辑信息数据库
	private static AlbumInfoDao mAlbumInfoDao;
	// 歌手信息数据库
	private static ArtistInfoDao mArtistInfoDao;
	// 文件夹信息数据库
	private static FolderInfoDao mFolderInfoDao;
	//我的收藏信息数据库
	private static FavoriteInfoDao mFavoriteDao;
	
	public static List<MusicInfo> queryFavorite(Context context) {
		if(mFavoriteDao == null) {
			mFavoriteDao = new FavoriteInfoDao(context);
		}
		return mFavoriteDao.getMusicInfo();
	}

	/**
	 * 获取包含音频文件的文件夹信息
	 * @param context
	 * @return
	 */
	public static List<FolderInfo> queryFolder(Context context) {
		if(mFolderInfoDao == null) {
			mFolderInfoDao = new FolderInfoDao(context);
		}
		SPStorage sp = new SPStorage(context);
		Uri uri = MediaStore.Files.getContentUri("external");
		ContentResolver cr = context.getContentResolver();
		StringBuilder mSelection = new StringBuilder(FileColumns.MEDIA_TYPE
				+ " = " + FileColumns.MEDIA_TYPE_AUDIO + " and " + "("
				+ FileColumns.DATA + " like'%.mp3' or " + Media.DATA
				+ " like'%.wma')");
		// 查询语句：检索出.mp3为后缀名，时长大于1分钟，文件大小大于1MB的媒体文件
		if(sp.getFilterSize()) {
			mSelection.append(" and " + Media.SIZE + " > " + FILTER_SIZE);
		}
		if(sp.getFilterTime()) {
			mSelection.append(" and " + Media.DURATION + " > " + FILTER_DURATION);
		}
		mSelection.append(") group by ( " + FileColumns.PARENT);
		if (mFolderInfoDao.hasData()) {
			return mFolderInfoDao.getFolderInfo();
		} else {
			List<FolderInfo> list = getFolderList(cr.query(uri, proj_folder, mSelection.toString(), null, null));
			mFolderInfoDao.saveFolderInfo(list);
			return list;
		}
	}

	/**
	 * 获取歌手信息
	 * @param context
	 * @return
	 */
	public static List<ArtistInfo> queryArtist(Context context) {
		if(mArtistInfoDao == null) {
			mArtistInfoDao = new ArtistInfoDao(context);
		}
		Uri uri = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI;
		ContentResolver cr = context.getContentResolver();
		if (mArtistInfoDao.hasData()) {
			return mArtistInfoDao.getArtistInfo();
		} else {
			List<ArtistInfo> list = getArtistList(cr.query(uri, proj_artist,
					null, null, MediaStore.Audio.Artists.NUMBER_OF_TRACKS
							+ " desc"));
			mArtistInfoDao.saveArtistInfo(list);
			return list;
		}
	}

	/**
	 * 获取专辑信息
	 * @param context
	 * @return
	 */
	public static List<AlbumInfo> queryAlbums(Context context) {
		if(mAlbumInfoDao == null) {
			mAlbumInfoDao = new AlbumInfoDao(context);
		}
		
		SPStorage sp = new SPStorage(context);
		
		Uri uri = Albums.EXTERNAL_CONTENT_URI;
		ContentResolver cr = context.getContentResolver();
		StringBuilder where = new StringBuilder(Albums._ID
				+ " in (select distinct " + Media.ALBUM_ID
				+ " from audio_meta where (1=1 ");
		
		if(sp.getFilterSize()) {
			where.append(" and " + Media.SIZE + " > " + FILTER_SIZE);
		}
		if(sp.getFilterTime()) {
			where.append(" and " + Media.DURATION + " > " + FILTER_DURATION);
		}
		where.append("))");

		if (mAlbumInfoDao.hasData()) {
			return mAlbumInfoDao.getAlbumInfo();
		} else {
			// Media.ALBUM_KEY 按专辑名称排序
			List<AlbumInfo> list = getAlbumList(cr.query(uri, proj_album,
					where.toString(), null, Media.ALBUM_KEY));
			mAlbumInfoDao.saveAlbumInfo(list);
			return list;
		}
	}

	/**
	 * 
	 * @param context
	 * @param from 不同的界面进来要做不同的查询
	 * @return
	 */
	public static List<MusicInfo> queryMusic(Context context, int from) {
		return queryMusic(context, null, null, from);
	}

	public static List<MusicInfo> queryMusic(Context context,
			String selections, String selection, int from) {
		if(mMusicInfoDao == null) {
			mMusicInfoDao = new MusicInfoDao(context);
		}
		SPStorage sp = new SPStorage(context);
		Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
		ContentResolver cr = context.getContentResolver();

		StringBuffer select = new StringBuffer(" 1=1 ");
		// 查询语句：检索出.mp3为后缀名，时长大于1分钟，文件大小大于1MB的媒体文件
		if(sp.getFilterSize()) {
			select.append(" and " + Media.SIZE + " > " + FILTER_SIZE);
		}
		if(sp.getFilterTime()) {
			select.append(" and " + Media.DURATION + " > " + FILTER_DURATION);
		}

		if (!TextUtils.isEmpty(selections)) {
			select.append(selections);
		}
		
		switch(from) {
		case START_FROM_LOCAL:
			if (mMusicInfoDao.hasData()) {
				return mMusicInfoDao.getMusicInfo();
			} else {
				List<MusicInfo> list = getMusicList(cr.query(uri, proj_music,
						select.toString(), null,
						MediaStore.Audio.Media.ARTIST_KEY));
				mMusicInfoDao.saveMusicInfo(list);
				return list;
			}
		case START_FROM_ARTIST:
			if (mMusicInfoDao.hasData()) {
				return mMusicInfoDao.getMusicInfoByType(selection,
						START_FROM_ARTIST);
			} else {
//				return getMusicList(cr.query(uri, proj_music,
//						select.toString(), null,
//						MediaStore.Audio.Media.ARTIST_KEY));
			}
		case START_FROM_ALBUM:
			if (mMusicInfoDao.hasData()) {
				return mMusicInfoDao.getMusicInfoByType(selection,
						START_FROM_ALBUM);
			}
		case START_FROM_FOLDER:
			if(mMusicInfoDao.hasData()) {
				return mMusicInfoDao.getMusicInfoByType(selection, START_FROM_FOLDER);
			}
			default:
				return null;
		}

	}

	public static ArrayList<MusicInfo> getMusicList(Cursor cursor) {
		if (cursor == null) {
			return null;
		}
		ArrayList<MusicInfo> musicList = new ArrayList<MusicInfo>();
		while (cursor.moveToNext()) {
			MusicInfo music = new MusicInfo();
			music.songId = cursor.getInt(cursor
					.getColumnIndex(MediaStore.Audio.Media._ID));
			music.albumId = cursor.getInt(cursor
					.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
			music.duration = cursor.getInt(cursor
					.getColumnIndex(MediaStore.Audio.Media.DURATION));
			music.musicName = cursor.getString(cursor
					.getColumnIndex(MediaStore.Audio.Media.TITLE));
			music.artist = cursor.getString(cursor
					.getColumnIndex(MediaStore.Audio.Media.ARTIST));
			
			String filePath = cursor.getString(cursor
					.getColumnIndex(MediaStore.Audio.Media.DATA));
			music.data = filePath;
			String folderPath = filePath.substring(0,
					filePath.lastIndexOf(File.separator));
			music.folder = folderPath;
			music.musicNameKey = StringHelper.getPingYin(music.musicName);
			music.artistKey = StringHelper.getPingYin(music.artist);
			musicList.add(music);
		}
		cursor.close();
		return musicList;
	}

	public static List<AlbumInfo> getAlbumList(Cursor cursor) {
		List<AlbumInfo> list = new ArrayList<AlbumInfo>();
		while (cursor.moveToNext()) {
			AlbumInfo info = new AlbumInfo();
			info.album_name = cursor.getString(cursor
					.getColumnIndex(Albums.ALBUM));
			info.album_id = cursor.getInt(cursor.getColumnIndex(Albums._ID));
			info.number_of_songs = cursor.getInt(cursor
					.getColumnIndex(Albums.NUMBER_OF_SONGS));
			info.album_art = cursor.getString(cursor
					.getColumnIndex(Albums.ALBUM_ART));
			list.add(info);
		}
		cursor.close();
		return list;
	}

	public static List<ArtistInfo> getArtistList(Cursor cursor) {
		List<ArtistInfo> list = new ArrayList<ArtistInfo>();
		while (cursor.moveToNext()) {
			ArtistInfo info = new ArtistInfo();
			info.artist_name = cursor.getString(cursor
					.getColumnIndex(MediaStore.Audio.Artists.ARTIST));
			info.number_of_tracks = cursor.getInt(cursor
					.getColumnIndex(MediaStore.Audio.Artists.NUMBER_OF_TRACKS));
			list.add(info);
		}
		cursor.close();
		return list;
	}

	public static List<FolderInfo> getFolderList(Cursor cursor) {
		List<FolderInfo> list = new ArrayList<FolderInfo>();
		while (cursor.moveToNext()) {
			FolderInfo info = new FolderInfo();
			String filePath = cursor.getString(cursor
					.getColumnIndex(MediaStore.Files.FileColumns.DATA));
			info.folder_path = filePath.substring(0,
					filePath.lastIndexOf(File.separator));
			info.folder_name = info.folder_path.substring(info.folder_path
					.lastIndexOf(File.separator) + 1);
			list.add(info);
		}
		cursor.close();
		return list;
	}

	public static String makeTimeString(long milliSecs) {
		StringBuffer sb = new StringBuffer();
		long m = milliSecs / (60 * 1000);
		sb.append(m < 10 ? "0" + m : m);
		sb.append(":");
		long s = (milliSecs % (60 * 1000)) / 1000;
		sb.append(s < 10 ? "0" + s : s);
		return sb.toString();
	}

	public static Bitmap getCachedArtwork(Context context, long artIndex,
			Bitmap defaultArtwork) {
		Bitmap bitmap = null;
		synchronized (sArtCache) {
			bitmap = sArtCache.get(artIndex);
		}
		if(context == null) {
			return null;
		}
		if (bitmap == null) {
			bitmap = defaultArtwork;
			int w = bitmap.getWidth();
			int h = bitmap.getHeight();
			Bitmap b = MusicUtils.getArtworkQuick(context, artIndex, w, h);
			if (b != null) {
				bitmap = b;
				synchronized (sArtCache) {
					// the cache may have changed since we checked
					Bitmap value = sArtCache.get(artIndex);
					if (value == null) {
						sArtCache.put(artIndex, bitmap);
					} else {
						bitmap = value;
					}
				}
			}
		}
		return bitmap;
	}

	// A really simple BitmapDrawable-like class, that doesn't do
	// scaling, dithering or filtering.
	/*
	 * private static class FastBitmapDrawable extends Drawable { private Bitmap
	 * mBitmap; public FastBitmapDrawable(Bitmap b) { mBitmap = b; }
	 * 
	 * @Override public void draw(Canvas canvas) { canvas.drawBitmap(mBitmap, 0,
	 * 0, null); }
	 * 
	 * @Override public int getOpacity() { return PixelFormat.OPAQUE; }
	 * 
	 * @Override public void setAlpha(int alpha) { }
	 * 
	 * @Override public void setColorFilter(ColorFilter cf) { } }
	 */

	// Get album art for specified album. This method will not try to
	// fall back to getting artwork directly from the file, nor will
	// it attempt to repair the database.
	public static Bitmap getArtworkQuick(Context context, long album_id, int w,
			int h) {
		// NOTE: There is in fact a 1 pixel border on the right side in the
		// ImageView
		// used to display this drawable. Take it into account now, so we don't
		// have to
		// scale later.
		w -= 1;
		ContentResolver res = context.getContentResolver();
		Uri uri = ContentUris.withAppendedId(sArtworkUri, album_id);
		if (uri != null) {
			ParcelFileDescriptor fd = null;
			try {
				fd = res.openFileDescriptor(uri, "r");
				int sampleSize = 1;

				// Compute the closest power-of-two scale factor
				// and pass that to sBitmapOptionsCache.inSampleSize, which will
				// result in faster decoding and better quality
				sBitmapOptionsCache.inJustDecodeBounds = true;
				BitmapFactory.decodeFileDescriptor(fd.getFileDescriptor(),
						null, sBitmapOptionsCache);
				int nextWidth = sBitmapOptionsCache.outWidth >> 1;
				int nextHeight = sBitmapOptionsCache.outHeight >> 1;
				while (nextWidth > w && nextHeight > h) {
					sampleSize <<= 1;
					nextWidth >>= 1;
					nextHeight >>= 1;
				}

				sBitmapOptionsCache.inSampleSize = sampleSize;
				sBitmapOptionsCache.inJustDecodeBounds = false;
				Bitmap b = BitmapFactory.decodeFileDescriptor(
						fd.getFileDescriptor(), null, sBitmapOptionsCache);

				if (b != null) {
					// finally rescale to exactly the size we need
					if (sBitmapOptionsCache.outWidth != w
							|| sBitmapOptionsCache.outHeight != h) {
						Bitmap tmp = Bitmap.createScaledBitmap(b, w, h, true);
						// Bitmap.createScaledBitmap() can return the same
						// bitmap
						if (tmp != b)
							b.recycle();
						b = tmp;
					}
				}

				return b;
			} catch (FileNotFoundException e) {
			} finally {
				try {
					if (fd != null)
						fd.close();
				} catch (IOException e) {
				}
			}
		}
		return null;
	}

	/**
	 * 根据歌曲的ID，寻找出歌曲在当前播放列表中的位置
	 * 
	 * @param list
	 * @param id
	 * @return
	 */
	public static int seekPosInListById(List<MusicInfo> list, int id) {
		if(id == -1) {
			return -1;
		}
		int result = -1;
		if (list != null) {

			for (int i = 0; i < list.size(); i++) {
				if (id == list.get(i).songId) {
					result = i;
					break;
				}
			}
		}
		return result;
	}

	/**
	 * Get album art for specified album. You should not pass in the album id
	 * for the "unknown" album here (use -1 instead) This method always returns
	 * the default album art icon when no album art is found.
	 */
	/*
	 * public static Bitmap getArtwork(Context context, long song_id, long
	 * album_id) { return getArtwork(context, song_id, album_id, true); }
	 *//**
	 * Get album art for specified album. You should not pass in the album id
	 * for the "unknown" album here (use -1 instead)
	 */
	/*
	 * public static Bitmap getArtwork(Context context, long song_id, long
	 * album_id, boolean allowdefault) {
	 * 
	 * // This is something that is not in the database, so get the album // art
	 * directly // from the file. if (song_id >= 0) { Bitmap bm =
	 * getArtworkFromFile(context, song_id, -1); if (bm != null) { return bm; }
	 * else { return getArtwork(context, -1, album_id); } } else if (album_id >=
	 * 0) {
	 * 
	 * ContentResolver res = context.getContentResolver(); Uri uri =
	 * ContentUris.withAppendedId(sArtworkUri, album_id); if (uri != null) {
	 * InputStream in = null; try { in = res.openInputStream(uri); return
	 * BitmapFactory.decodeStream(in, null, sBitmapOptions); } catch
	 * (FileNotFoundException ex) { // The album art thumbnail does not actually
	 * exist. Maybe // the // user deleted it, or // maybe it never existed to
	 * begin with. Bitmap bm = getArtworkFromFile(context, song_id, album_id);
	 * if (bm != null) { if (bm.getConfig() == null) { bm =
	 * bm.copy(Bitmap.Config.RGB_565, false); if (bm == null && allowdefault) {
	 * return getDefaultArtwork(context); } } } else if (allowdefault) { bm =
	 * getDefaultArtwork(context); } return bm; } finally { try { if (in !=
	 * null) { in.close(); } } catch (IOException ex) { } } }
	 * 
	 * }
	 * 
	 * return null; }
	 * 
	 * // get album art for specified file private static final String
	 * sExternalMediaUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
	 * .toString(); private static Bitmap mCachedBit = null;
	 * 
	 * private static Bitmap getArtworkFromFile(Context context, long songid,
	 * long albumid) { Bitmap bm = null; byte[] art = null; String path = null;
	 * 
	 * if (albumid < 0 && songid < 0) { throw new IllegalArgumentException(
	 * "Must specify an album or a song id"); }
	 * 
	 * try { if (songid >= 0) { Uri uri =
	 * Uri.parse("content://media/external/audio/media/" + songid +
	 * "/albumart"); ParcelFileDescriptor pfd = context.getContentResolver()
	 * .openFileDescriptor(uri, "r"); if (pfd != null) { FileDescriptor fd =
	 * pfd.getFileDescriptor(); bm = BitmapFactory.decodeFileDescriptor(fd); }
	 * else { return getArtworkFromFile(context, -1, albumid); } } else if
	 * (albumid >= 0) { Uri uri = ContentUris.withAppendedId(sArtworkUri,
	 * albumid); ParcelFileDescriptor pfd = context.getContentResolver()
	 * .openFileDescriptor(uri, "r"); if (pfd != null) { FileDescriptor fd =
	 * pfd.getFileDescriptor(); bm = BitmapFactory.decodeFileDescriptor(fd); } }
	 * } catch (IllegalStateException ex) { } catch (FileNotFoundException ex) {
	 * } if (bm != null) { mCachedBit = bm; } return bm; }
	 * 
	 * private static Bitmap getDefaultArtwork(Context context) {
	 * BitmapFactory.Options opts = new BitmapFactory.Options();
	 * opts.inPreferredConfig = Bitmap.Config.ARGB_8888; return
	 * BitmapFactory.decodeStream(context.getResources()
	 * .openRawResource(R.drawable.img_album_background), null, opts); }
	 */

	public static void clearCache() {
		sArtCache.clear();
	}
}
