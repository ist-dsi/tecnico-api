const buildSidebar = (gridsomeActions) => {
  const servicesCollection = gridsomeActions.getCollection('MarkdownPage')
  const allServices = servicesCollection.findNodes().map((service) => ({ title: service.title, path: service.path.split('/').filter((v) => v !== '') }))

  const isParentPath = (child, parent) => child.length === parent.length + 1 && parent.every((el, i) => el === child[i])

  allServices.forEach((service) => {
    // No need to find the parent of root services
    if (service.path.length === 1) return

    const parent = allServices.find(({ path }) => isParentPath(service.path, path))

    if (!parent) {
      console.error(`Cannot find parent service of ${service.path.join('/')}`)
      return
    }

    const children = parent.children || (parent.children = [])
    children.push(service)
  })

  const compFn = (a, b) => {
    const nameA = a.title.toUpperCase() // ignore upper and lowercase
    const nameB = b.title.toUpperCase() // ignore upper and lowercase
    if (nameA < nameB) {
      return -1
    }
    if (nameA > nameB) {
      return 1
    }
    return 0
  }

  const regeneratePathAndSort = (service) => {
    service.path = `/${service.path.join('/')}`
    if (service.children) {
      service.children.forEach(regeneratePathAndSort)
      service.children = service.children.sort(compFn)
    }
    return service
  }
  return [
    {
      title: 'Home Page',
      path: '/'
    },
    ...allServices
      .filter((service) => service.path.length === 1)
      .map(regeneratePathAndSort)
      .sort(compFn),
    {
      title: 'GitHub Repository',
      path: 'https://github.com/ist-dsi/fenixedu-api/'
    },
    {
      title: 'FenixEdu API (v1) Docs',
      path: 'https://fenixedu.org/dev/api/'
    },
    {
      title: 'Técnico API (v2) Docs',
      path: '/openapi/v2/'
    }
  ]
}

module.exports = {
  buildSidebar
}
