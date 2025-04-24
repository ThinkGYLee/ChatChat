package com.gyleedev.chatchat.data.database

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.gyleedev.chatchat.data.database.dao.UserAndFavoriteDao
import com.gyleedev.chatchat.data.database.entity.toLocalData
import com.gyleedev.chatchat.ui.friendlist.FriendListUiState
import com.gyleedev.chatchat.util.PreferenceUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException

/**
 * 데이터 식별자와 데이터 타입을 통해 DB의 데이터를 로드하는 곳
 */
class FriendListPagingSource(
    private val userAndFavoriteDao: UserAndFavoriteDao,
    private val preferenceUtil: PreferenceUtil
) : PagingSource<Int, FriendListUiState>() {
    /**
     * 스크롤 할 때마다 데이터를 비동기적으로 가져오는 메서드
     */

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, FriendListUiState> {
        // 시작 페이지
        // 처음에 null 값인 것을 고려하여 시작 값 부여
        val page = params.key ?: STARTING_PAGE

        return try {
            var data: MutableList<FriendListUiState> = mutableListOf()

            // TODO 즐겨찾기 항목도 자동적으로 추가 될수 있는 로직 고려해볼것
            // page 값에 따른 list 호출
            // join을 사용해서 list 값을 저장
            CoroutineScope(Dispatchers.IO).launch {
                if (page == 1) {
                    data = mutableListOf(
                        FriendListUiState.MyData(
                            preferenceUtil.getMyData()
                        )
                    )
                    val favorites = userAndFavoriteDao.getFavorites()
                    data.add(FriendListUiState.Title("즐겨찾기 ${favorites.size}"))
                    data.addAll(favorites.map { FriendListUiState.FavoriteData(it.toLocalData()) })
                    data.add(FriendListUiState.Title("친구 ${userAndFavoriteDao.getFriendsCount()}"))
                }

                data.addAll(
                    userAndFavoriteDao.getFriendsAndFavoritesWithPage(page)
                        .map { FriendListUiState.FriendData(it.toLocalData()) }
                )
                // data = SampleDatabase.sampleDB!!.getSampleDao().getList(page)
            }.join()

            // 반환할 데이터
            LoadResult.Page(
                data = data,
                prevKey = if (page == STARTING_PAGE) null else page - 1,
                nextKey = if (data.isEmpty()) null else page + 1
            )
        } catch (e: IOException) {
            LoadResult.Error(e)
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    /**
     * 현재 목록을 대체할 새 데이터를 로드할 때 사용
     */
    override fun getRefreshKey(state: PagingState<Int, FriendListUiState>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}

private const val STARTING_PAGE = 1 // 초기 페이지 상수 값
