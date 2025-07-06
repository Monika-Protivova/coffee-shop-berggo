package com.motycka.edu.menu

import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

class InternalMenuService(
    private val menuRepository: MenuRepository
) {

    suspend fun getMenuItems(ids: Set<MenuItemId>): Set<MenuItemDTO> {
        logger.debug { "Getting menu items with ids: $ids" }
        return menuRepository.selectMenuItems(
            filter = null,
            ids = ids
        )
    }

    suspend fun getMenuItemsByIds(ids: List<MenuItemId>): List<MenuItemDTO> {
        logger.debug { "Getting menu items with ids: $ids" }
        return menuRepository.selectMenuItems(
            filter = null,
            ids = ids.toSet()
        ).toList()
    }
}