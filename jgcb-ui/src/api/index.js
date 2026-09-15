import axios from 'axios'
import { backendHttpOrigin } from '../utils/runtime'

const request = axios.create({
  baseURL: backendHttpOrigin() + '/api',
  timeout: 10000
})

request.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  response => response.data,
  error => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export function login(data) {
  return request.post('/auth/login', data)
}

export function register(data) {
  return request.post('/auth/register', data)
}

export function getUserInfo() {
  return request.get('/user/info')
}

export function getUserProfile(id) {
  return request.get(`/user/${id}`)
}

export function updatePassword(data) {
  return request.put('/user/password', data)
}

export function updateAvatar(data) {
  return request.put('/user/avatar', data)
}

export function updateProfile(data) {
  return request.put('/user/profile', data)
}

export function getMembers() {
  return request.get('/user/list')
}

export function uploadFile(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/file/upload', formData, { timeout: 600000 })
}

export function uploadFiles(files) {
  const formData = new FormData()
  files.forEach(f => formData.append('files', f))
  return request.post('/file/uploads', formData, { timeout: 600000 })
}

export function createSession(data) {
  return request.post('/mahjong/sessions', data)
}

export function getSessions(page = 1, size = 10) {
  return request.get('/mahjong/sessions', { params: { page, size } })
}

export function joinSession(id) {
  return request.post(`/mahjong/sessions/${id}/join`)
}

export function leaveSession(id) {
  return request.post(`/mahjong/sessions/${id}/leave`)
}

export function cancelSession(id) {
  return request.post(`/mahjong/sessions/${id}/cancel`)
}

export function getSessionDetail(id) {
  return request.get(`/mahjong/sessions/${id}`)
}

export function settleSession(id, data) {
  return request.post(`/mahjong/sessions/${id}/settle`, data)
}

export function getMahjongStats() {
  return request.get('/mahjong/stats')
}

export function createBilliard(data) {
  return request.post('/billiard/sessions', data)
}

export function getBilliards(page = 1, size = 10) {
  return request.get('/billiard/sessions', { params: { page, size } })
}

export function getBilliardDetail(id) {
  return request.get(`/billiard/sessions/${id}`)
}

export function joinBilliard(id) {
  return request.post(`/billiard/sessions/${id}/join`)
}

export function leaveBilliard(id) {
  return request.post(`/billiard/sessions/${id}/leave`)
}

export function cancelBilliard(id) {
  return request.post(`/billiard/sessions/${id}/cancel`)
}

export function settleBilliard(id, data) {
  return request.post(`/billiard/sessions/${id}/settle`, data)
}

export function createBoardGame(data) {
  return request.post('/boardgame/sessions', data)
}

export function getBoardGames(page = 1, size = 10) {
  return request.get('/boardgame/sessions', { params: { page, size } })
}

export function getBoardGameDetail(id) {
  return request.get(`/boardgame/sessions/${id}`)
}

export function joinBoardGame(id) {
  return request.post(`/boardgame/sessions/${id}/join`)
}

export function leaveBoardGame(id) {
  return request.post(`/boardgame/sessions/${id}/leave`)
}

export function cancelBoardGame(id) {
  return request.post(`/boardgame/sessions/${id}/cancel`)
}

export function settleBoardGame(id, data) {
  return request.post(`/boardgame/sessions/${id}/settle`, data)
}

export function createMemoir(data) {
  return request.post('/memoirs', data)
}

export function getMemoirs(page = 1, size = 10) {
  return request.get('/memoirs', { params: { page, size } })
}

export function getMemoirDetail(id) {
  return request.get(`/memoirs/${id}`)
}

export function deleteMemoir(id) {
  return request.delete(`/memoirs/${id}`)
}

export function likeMemoir(id) {
  return request.post(`/memoirs/${id}/like`)
}

export function getMemoirComments(id) {
  return request.get(`/memoirs/${id}/comments`)
}

export function addMemoirComment(id, content) {
  return request.post(`/memoirs/${id}/comments`, { content })
}

export function deleteMemoirComment(memoirId, commentId) {
  return request.delete(`/memoirs/${memoirId}/comments/${commentId}`)
}

export function createKaraoke(data) {
  return request.post('/karaoke/sessions', data)
}

export function getKaraokes(page = 1, size = 10) {
  return request.get('/karaoke/sessions', { params: { page, size } })
}

export function getKaraokeDetail(id) {
  return request.get(`/karaoke/sessions/${id}`)
}

export function joinKaraoke(id) {
  return request.post(`/karaoke/sessions/${id}/join`)
}

export function leaveKaraoke(id) {
  return request.post(`/karaoke/sessions/${id}/leave`)
}

export function cancelKaraoke(id) {
  return request.post(`/karaoke/sessions/${id}/cancel`)
}

export function settleKaraoke(id, data) {
  return request.post(`/karaoke/sessions/${id}/settle`, data)
}

export function createMovie(data) {
  return request.post('/movie/sessions', data)
}

export function getMovies(page = 1, size = 10) {
  return request.get('/movie/sessions', { params: { page, size } })
}

export function getMovieDetail(id) {
  return request.get(`/movie/sessions/${id}`)
}

export function joinMovie(id) {
  return request.post(`/movie/sessions/${id}/join`)
}

export function leaveMovie(id) {
  return request.post(`/movie/sessions/${id}/leave`)
}

export function cancelMovie(id) {
  return request.post(`/movie/sessions/${id}/cancel`)
}

export function uploadMovieImages(id, data) {
  return request.post(`/movie/sessions/${id}/images`, data)
}

export function finishMovie(id) {
  return request.post(`/movie/sessions/${id}/finish`)
}

export function createTravel(data) {
  return request.post('/travels', data)
}

export function getTravels(page = 1, size = 10) {
  return request.get('/travels', { params: { page, size } })
}

export function getTravelDetail(id) {
  return request.get(`/travels/${id}`)
}

export function deleteTravel(id) {
  return request.delete(`/travels/${id}`)
}

export function doCheckIn() {
  return request.post('/checkin')
}

export function getCheckInStatus() {
  return request.get('/checkin/status')
}

export function getCheckInCalendar(year, month) {
  return request.get('/checkin/calendar', { params: { year, month } })
}

export function getPetStatus() {
  return request.get('/pet/status')
}

export function getPetStatusOf(userId) {
  return request.get(`/pet/status/${userId}`)
}

export function updatePetOutfit(data) {
  return request.put('/pet/outfit', data)
}

export function getRoom(roomId = 1) {
  return request.get(`/rooms/${roomId}`)
}

export function getRoomPlayers(roomId = 1) {
  return request.get(`/rooms/${roomId}/players`)
}

export function saveRoomPlayerState(roomId, data) {
  return request.put(`/rooms/${roomId}/players/me/state`, data)
}

export function saveRoomObjectState(roomId, objectId, data) {
  return request.put(`/rooms/${roomId}/objects/${objectId}/state`, data)
}