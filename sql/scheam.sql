DROP TABLE IF EXISTS OutboxEvent;
DROP TABLE IF EXISTS SagaState;

CREATE TABLE `OutboxEvent` (
  `id` varchar(255) NOT NULL,
  `aggregateId` varchar(255) DEFAULT NULL,
  `aggregateType` varchar(255) DEFAULT NULL,
  `createdAt` varchar(255) DEFAULT NULL,
  `eventType` varchar(255) DEFAULT NULL,
  `payload` longtext DEFAULT NULL,
  `payloadType` varchar(255) DEFAULT NULL,
  `sentAt` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `version` bigint(20) NOT NULL,
  `additionalInfo` varchar(255) DEFAULT NULL,
  `topicPrefix` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE `SagaState` (
  `id` varchar(255) NOT NULL,
  `currentStep` varchar(255) DEFAULT NULL,
  `objectId` varchar(255) DEFAULT NULL,
  `payload` longtext DEFAULT NULL,
  `payloadType` varchar(255) DEFAULT NULL,
  `sagaStateStatus` varchar(255) DEFAULT NULL,
  `stepStatus` varchar(255) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `version` bigint(20) NOT NULL,
  `createdAt` varchar(255) DEFAULT NULL,
  `updatedAt` varchar(255) DEFAULT NULL,
  `sagaStateDetailed` longtext DEFAULT NULL,
  `cachedData` longtext DEFAULT NULL,
  `cachedType` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
