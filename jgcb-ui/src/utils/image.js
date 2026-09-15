export function thumbUrl(url, w = 600) {
  if (!url) return url
  return `/api/file/thumb?url=${encodeURIComponent(url)}&w=${w}`
}

export function isVideo(url) {
  if (!url) return false
  const clean = url.split('?')[0].split('#')[0]
  return /\.(mp4|webm|mov|m4v)$/i.test(clean)
}
